package com.emarsys.escher;


import java.net.URI;
import java.util.List;

public interface EscherRequest {

    public String getHttpMethod();
    public URI getURI();
    public List<Header> getRequestHeaders();
    public void addHeader(String fieldName, String fieldValue);
    public String getBody();


    public static class Header {
        private String fieldName;
        private String fieldValue;

        public Header(String fieldName, String fieldValue) {
            this.fieldName = fieldName;
            this.fieldValue = fieldValue;
        }


        public String getFieldName() {
            return fieldName;
        }


        public String getFieldValue() {
            return fieldValue;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Header header = (Header) o;

            return fieldName.equals(header.fieldName) && fieldValue.equals(header.fieldValue);

        }


        @Override
        public int hashCode() {
            int result = fieldName.hashCode();
            result = 31 * result + fieldValue.hashCode();
            return result;
        }


        @Override
        public String toString() {
            return fieldName + "=" + fieldValue;
        }
    }

}
