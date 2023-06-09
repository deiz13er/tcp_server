package com.example.ISO8583.interfaces;

import com.example.ISO8583.entities.ISOMessage;
import com.example.ISO8583.enums.FIELDS;
import com.example.ISO8583.exceptions.ISOException;
import com.example.ISO8583.security.ISOMacGenerator;


public interface DataElement<T> {

    ISOMessage build() throws ISOException;

    DataElement<T> generateMac(ISOMacGenerator generator) throws ISOException;

    DataElement<T> setField(int no, String value) throws ISOException;
    DataElement<T> setField(FIELDS field, String value) throws ISOException;
    DataElement<T> setField(int no, byte[] value) throws ISOException;
    DataElement<T> setField(FIELDS field, byte[] value) throws ISOException;


    DataElement<T> setHeader(String header);
}
