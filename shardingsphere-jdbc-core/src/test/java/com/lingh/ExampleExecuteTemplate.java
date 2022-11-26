

package com.lingh;

import com.lingh.service.ExampleService;

import java.sql.SQLException;

public final class ExampleExecuteTemplate {

    public static void run(final ExampleService exampleService) throws SQLException {
        try {
            exampleService.initEnvironment();
            exampleService.processSuccess();
        } finally {
            exampleService.cleanEnvironment();
        }
    }

    public static void runFailure(final ExampleService exampleService) throws SQLException {
        try {
            exampleService.initEnvironment();
            exampleService.processFailure();
        } finally {
            exampleService.cleanEnvironment();
        }
    }
}
