package com.exequt.ecommerce.application.exception;

import com.exequt.ecommerce.domain.model.InvalidStateTransitionException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Resource Not Found");
        return pd;
    }

    @ExceptionHandler(CartLockedException.class)
    public ProblemDetail handleCartLocked(CartLockedException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Cart Locked");
        return pd;
    }

    @ExceptionHandler(CartEmptyException.class)
    public ProblemDetail handleCartEmpty(CartEmptyException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Cart Empty");
        return pd;
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ProblemDetail handleInvalidStateTransition(InvalidStateTransitionException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Invalid State Transition");
        return pd;
    }

    @ExceptionHandler(DuplicatePaymentException.class)
    public ProblemDetail handleDuplicatePayment(DuplicatePaymentException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Duplicate Payment");
        return pd;
    }

    @ExceptionHandler(IdempotencyKeyMismatchException.class)
    public ProblemDetail handleIdempotencyMismatch(IdempotencyKeyMismatchException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Idempotency Key Mismatch");
        return pd;
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFound(ProductNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Product Not Found");
        return pd;
    }

    @ExceptionHandler(ProductUnavailableException.class)
    public ProblemDetail handleProductUnavailable(ProductUnavailableException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Product Unavailable");
        return pd;
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Concurrent update conflict. Please retry.");
        pd.setTitle("Concurrent Update Conflict");
        return pd;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Data integrity violation. Duplicate payment request.");
        pd.setTitle("Data Integrity Violation");
        return pd;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Invalid Operation");
        return pd;
    }
}
