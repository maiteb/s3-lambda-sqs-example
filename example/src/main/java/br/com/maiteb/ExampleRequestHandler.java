package br.com.maiteb;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class ExampleRequestHandler implements RequestHandler<Object, Object> {

    private static final LambdaLogger logger = new LambdaLogger() {

        public void log(String string) {
            System.out.println(string);
        }
    };

    public Object handleRequest(Object input, Context context) {
        logger.log("Lambda executado com sucesso");
        return "success";
    }

}
