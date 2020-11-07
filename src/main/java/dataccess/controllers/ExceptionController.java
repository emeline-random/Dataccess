package dataccess.controllers;

import dataccess.dao.DaoAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice(basePackages = "dataccess.controllers")
public class ExceptionController { //FIXME


    @ExceptionHandler
    public String handleAllExceptions(Exception ex) {
        System.out.println("Exception Occured: " + ex);
        return "404";
    }

    @ExceptionHandler(DaoAccessException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(Exception ex) {
        System.out.println("Exception Occured: " + ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Exception: " + ex.getLocalizedMessage());
    }

}
