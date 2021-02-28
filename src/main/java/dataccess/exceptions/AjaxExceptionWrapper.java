package dataccess.exceptions;

import org.omnifaces.exceptionhandler.FullAjaxExceptionHandler;
import org.slf4j.LoggerFactory;

import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

/**
 * Exception handler for exceptions that occurre during an ajax request
 */
public class AjaxExceptionWrapper extends FullAjaxExceptionHandler {

    @Override
    public void handle() throws FacesException {
        Iterator<ExceptionQueuedEvent> unhandledExceptionQueuedEvents = getUnhandledExceptionQueuedEvents().iterator();
        if (unhandledExceptionQueuedEvents.hasNext()) {
            Throwable exception = unhandledExceptionQueuedEvents.next().getContext().getException();
            logException(exception);
        }
        super.handle();
    }

    private void logException(Throwable exception) {
        String username = ((HttpServletRequest)
                FacesContext.getCurrentInstance().getExternalContext().getRequest()).getUserPrincipal().getName();
        LoggerFactory.getLogger(AjaxExceptionWrapper.class)
                .error("an error has occurred from AjaxExceptionWrapper.handle for user "
                        + username , exception);
    }

    public AjaxExceptionWrapper(ExceptionHandler wrapped) {
        super(wrapped);
    }
}