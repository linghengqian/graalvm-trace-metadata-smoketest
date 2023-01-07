package io.etcd.jetcd.impl;

import io.etcd.jetcd.Client;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.api.LeaseGrpc.LeaseImplBase;
import io.etcd.jetcd.api.LeaseKeepAliveRequest;
import io.etcd.jetcd.api.LeaseKeepAliveResponse;
import io.etcd.jetcd.support.CloseableClient;
import io.etcd.jetcd.support.Observers;
import io.etcd.jetcd.test.GrpcServerExtension;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.etcd.jetcd.common.exception.EtcdExceptionFactory.toEtcdException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@Disabled("TODO: does not seems to add much value")
@Timeout(value = 1, unit = TimeUnit.MINUTES)
@ExtendWith(MockitoExtension.class)
public class LeaseUnitTest {

    private Lease leaseCli;
    private AtomicReference<StreamObserver<LeaseKeepAliveResponse>> responseObserverRef;
    private static final long LEASE_ID_1 = 1;
    private static final long LEASE_ID_2 = 2;

    @RegisterExtension
    public final GrpcServerExtension grpcServerRule = new GrpcServerExtension().directExecutor();

    @Mock
    private StreamObserver<LeaseKeepAliveRequest> requestStreamObserverMock;

    @BeforeEach
    public void setup() {
        this.responseObserverRef = new AtomicReference<>();
        this.grpcServerRule.getServiceRegistry()
                .addService(this.createLeaseImplBase(this.responseObserverRef, this.requestStreamObserverMock));

        this.leaseCli = new LeaseImpl(new ClientConnectionManager(Client.builder(), grpcServerRule.getChannel()));
    }

    @AfterEach
    public void tearDown() {
        this.leaseCli.close();
        this.grpcServerRule.getServer().shutdownNow();
    }

    @Test
    public void testKeepAliveOnce() throws ExecutionException, InterruptedException {
        CompletableFuture<io.etcd.jetcd.lease.LeaseKeepAliveResponse> lrpFuture = this.leaseCli.keepAliveOnce(LEASE_ID_1);
        LeaseKeepAliveResponse lrp = LeaseKeepAliveResponse.newBuilder().setID(LEASE_ID_1).build();
        this.responseObserverRef.get().onNext(lrp);

        io.etcd.jetcd.lease.LeaseKeepAliveResponse lrpActual = lrpFuture.get();
        assertThat(lrpActual.getID()).isEqualTo(lrp.getID());
    }

    @Test
    public void testKeepAliveOnceConnectError() {
        CompletableFuture<io.etcd.jetcd.lease.LeaseKeepAliveResponse> lrpFuture = this.leaseCli.keepAliveOnce(LEASE_ID_1);
        Throwable t = Status.ABORTED.asRuntimeException();
        responseObserverRef.get().onError(t);
        assertThatThrownBy(lrpFuture::get).hasCause(toEtcdException(t));
    }

    @Test
    public void testKeepAliveOnceStreamCloseOnSuccess() throws ExecutionException, InterruptedException {
        CompletableFuture<io.etcd.jetcd.lease.LeaseKeepAliveResponse> lrpFuture = this.leaseCli.keepAliveOnce(LEASE_ID_1);
        LeaseKeepAliveResponse lrp = LeaseKeepAliveResponse.newBuilder().setID(LEASE_ID_1).build();
        this.responseObserverRef.get().onNext(lrp);
        lrpFuture.get();
        verify(this.requestStreamObserverMock, timeout(100).times(1)).onCompleted();
    }

    @Test
    public void testKeepAliveOnSendingKeepAliveRequests() {
        final StreamObserver<io.etcd.jetcd.lease.LeaseKeepAliveResponse> observer = Observers.observer(response -> {
        });
        try (CloseableClient ignored = this.leaseCli.keepAlive(LEASE_ID_1, observer)) {
            verify(this.requestStreamObserverMock, timeout(1100).atLeast(2)).onNext(argThat(hasLeaseID(LEASE_ID_1)));
        }
    }

    @Test
    public void testKeepAliveAfterFirstKeepAliveTimeout() throws InterruptedException {
        final StreamObserver<io.etcd.jetcd.lease.LeaseKeepAliveResponse> observer = Observers.observer(response -> {
        });
        try (CloseableClient ignored = this.leaseCli.keepAlive(LEASE_ID_1, observer)) {
            verify(this.requestStreamObserverMock, after(11000).atLeastOnce()).onNext(argThat(hasLeaseID(LEASE_ID_1)));
            Mockito.<StreamObserver>reset(this.requestStreamObserverMock);
            verify(this.requestStreamObserverMock, after(1000).times(0)).onNext(argThat(hasLeaseID(LEASE_ID_1)));
        }
    }

    @Test
    @SuppressWarnings("FutureReturnValueIgnored")
    public void testTimeToLiveNullOption() {
        assertThatThrownBy(() -> this.leaseCli.timeToLive(LEASE_ID_1, null)).isInstanceOf(NullPointerException.class)
                .hasMessage("LeaseOption should not be null");
    }

    @Test
    public void testKeepAliveCloseOnlyListener() {
        final StreamObserver<io.etcd.jetcd.lease.LeaseKeepAliveResponse> observer = Observers.observer(response -> {
        });
        final CloseableClient client = this.leaseCli.keepAlive(LEASE_ID_1, observer);
        client.close();
        verify(this.requestStreamObserverMock, after(1000).atMost(1)).onNext(argThat(hasLeaseID(LEASE_ID_1)));
    }

    @SuppressWarnings("PMD.UnusedLocalVariable")
    @Test
    public void testKeepAliveCloseSomeListeners() {
        final StreamObserver<io.etcd.jetcd.lease.LeaseKeepAliveResponse> observer = Observers.observer(response -> {
        });
        final CloseableClient client1 = this.leaseCli.keepAlive(LEASE_ID_2, observer);
        final CloseableClient client2 = this.leaseCli.keepAlive(LEASE_ID_1, observer);
        client1.close();
        verify(this.requestStreamObserverMock, after(1200).atLeast(2)).onNext(argThat(hasLeaseID(LEASE_ID_1)));
        client2.close();
    }

    @Test
    public void testKeepAliveResetOnStreamErrors() {
        final StreamObserver<io.etcd.jetcd.lease.LeaseKeepAliveResponse> observer = Observers.observer(response -> {
        });
        try (CloseableClient ignored = this.leaseCli.keepAlive(LEASE_ID_1, observer)) {
            Throwable t = Status.ABORTED.asRuntimeException();
            responseObserverRef.get().onError(t);
            verify(this.requestStreamObserverMock, timeout(2000).atLeast(3)).onNext(argThat(hasLeaseID(LEASE_ID_1)));
        }
    }

    private static ArgumentMatcher<LeaseKeepAliveRequest> hasLeaseID(long leaseId) {
        return o -> o.getID() == leaseId;
    }

    private static LeaseImplBase createLeaseImplBase(
            AtomicReference<StreamObserver<LeaseKeepAliveResponse>> responseObserverRef,
            StreamObserver<LeaseKeepAliveRequest> requestStreamObserver) {
        return new LeaseImplBase() {
            @Override
            public StreamObserver<LeaseKeepAliveRequest> leaseKeepAlive(
                    StreamObserver<LeaseKeepAliveResponse> responseObserver) {
                responseObserverRef.set(responseObserver);
                return requestStreamObserver;
            }
        };
    }
}
