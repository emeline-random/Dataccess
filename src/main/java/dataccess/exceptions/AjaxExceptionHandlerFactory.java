package dataccess.exceptions;

import org.omnifaces.exceptionhandler.FullAjaxExceptionHandlerFactory;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

/**
 * Factory declared in faces-config, this factory allows to register the
 * exception wrapper for ajax request exceptions
 */
public class AjaxExceptionHandlerFactory extends FullAjaxExceptionHandlerFactory {

    public AjaxExceptionHandlerFactory(ExceptionHandlerFactory wrapped) {
        super(wrapped);
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return new AjaxExceptionWrapper(super.getExceptionHandler());
    }
}
