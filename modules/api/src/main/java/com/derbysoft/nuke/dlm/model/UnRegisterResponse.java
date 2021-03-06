package com.derbysoft.nuke.dlm.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Created by passyt on 16-9-3.
 */
public class UnRegisterResponse extends BaseResponse {

    private boolean successful;

    public UnRegisterResponse() {
    }

    public UnRegisterResponse(String resourceId, boolean successful, Header header) {
        super(resourceId, header);
        this.successful = successful;
    }

    public UnRegisterResponse(String resourceId, String errorMessage, Header header) {
        super(resourceId, errorMessage, header);
    }

    public UnRegisterResponse(String resourceId, String errorMessage, boolean successful, Header header) {
        super(resourceId, errorMessage, header);
        this.successful = successful;
    }


    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnRegisterResponse)) return false;
        if (!super.equals(o)) return false;
        UnRegisterResponse that = (UnRegisterResponse) o;
        return successful == that.successful;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), successful);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", header)
                .add("resourceId", resourceId)
                .add("errorMessage", errorMessage)
                .add("successful", successful)
                .toString();
    }
}
