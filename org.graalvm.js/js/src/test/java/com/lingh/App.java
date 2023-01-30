package com.lingh;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.IOException;

@SuppressWarnings({"ThrowablePrintedToSystemOut", "unused", "RedundantThrows"})
public class App {

    public static final int WARMUP = 30;
    public static final int ITERATIONS = 10;
    public static final String BENCHFILE = "src/bench.js";

    public static final String SOURCE = """
            var N = 2000;
            var EXPECTED = 17393;

            function Natural() {
                x = 2;
                return {
                    'next' : function() { return x++; }
                };
            }

            function Filter(number, filter) {
                var self = this;
                this.number = number;
                this.filter = filter;
                this.accept = function(n) {
                  var filter = self;
                  for (;;) {
                      if (n % filter.number === 0) {
                          return false;
                      }
                      filter = filter.filter;
                      if (filter === null) {
                          break;
                      }
                  }
                  return true;
                };
                return this;
            }

            function Primes(natural) {
                var self = this;
                this.natural = natural;
                this.filter = null;

                this.next = function() {
                    for (;;) {
                        var n = self.natural.next();
                        if (self.filter === null || self.filter.accept(n)) {
                            self.filter = new Filter(n, self.filter);
                            return n;
                        }
                    }
                };
            }

            function primesMain() {
                var primes = new Primes(Natural());
                var primArray = [];
                for (var i=0;i<=N;i++) { primArray.push(primes.next()); }
                if (primArray[N] != EXPECTED) { throw new Error('wrong prime found: '+primArray[N]); }
            }
            """;

    public static void main(String[] args) throws Exception {
        benchGraalPolyglotContext();
        benchGraalScriptEngine();
        benchNashornScriptEngine();
    }

    static long benchGraalPolyglotContext() throws IOException {
        System.out.println("=== Graal.js via org.graalvm.polyglot.Context === ");
        long sum = 0;
        try (Context context = Context.create()) {
            context.eval(Source.newBuilder("js", SOURCE, "src.js").build());
            Value primesMain = context.getBindings("js").getMember("primesMain");
            System.out.println("warming up ...");
            for (int i = 0; i < WARMUP; i++) {
                primesMain.execute();
            }
            System.out.println("warmup finished, now measuring");
            for (int i = 0; i < ITERATIONS; i++) {
                long start = System.currentTimeMillis();
                primesMain.execute();
                long took = System.currentTimeMillis() - start;
                sum += took;
                System.out.println("iteration: " + took);
            }
        } // context.close() is automatic
        return sum;
    }

    static long benchNashornScriptEngine() throws IOException {
        System.out.println("=== Nashorn via javax.script.ScriptEngine ===");
        ScriptEngine nashornEngine = new ScriptEngineManager().getEngineByName("nashorn");
        if (nashornEngine == null) {
            System.out.println("*** Nashorn not found ***");
            return 0;
        } else {
            return benchScriptEngineIntl(nashornEngine);
        }
    }

    static long benchGraalScriptEngine() throws IOException {
        System.out.println("=== Graal.js via javax.script.ScriptEngine ===");
        ScriptEngine graaljsEngine = new ScriptEngineManager().getEngineByName("graal.js");
        if (graaljsEngine == null) {
            System.out.println("*** Graal.js not found ***");
            return 0;
        } else {
            return benchScriptEngineIntl(graaljsEngine);
        }
    }

    private static long benchScriptEngineIntl(ScriptEngine eng) throws IOException {
        long sum = 0L;
        try {
            eng.eval(SOURCE);
            Invocable inv = (Invocable) eng;
            System.out.println("warming up ...");
            for (int i = 0; i < WARMUP; i++) {
                inv.invokeFunction("primesMain");
            }
            System.out.println("warmup finished, now measuring");
            for (int i = 0; i < ITERATIONS; i++) {
                long start = System.currentTimeMillis();
                inv.invokeFunction("primesMain");
                long took = System.currentTimeMillis() - start;
                sum += took;
                System.out.println("iteration: " + (took));
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return sum;
    }

}
