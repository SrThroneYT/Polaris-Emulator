package com.websockets.plugin.utilities;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eu.habbo.Emulator;
import com.eu.habbo.plugin.EventListener;

public class Utilities implements EventListener {

    Logger LOGGER = LoggerFactory.getLogger(Emulator.class);

    public Utilities() {

    }

    public void LOGGER(String data) {
        LOGGER.info(data);
    }

    public void LOGGER_ERROR(String data) {
        LOGGER.error(data);
    }

    public String encode(String s) {
        String respuesta = "error";
        try {
            respuesta = new String(Base64.getEncoder().encode(s.getBytes("UTF8")), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return respuesta;
    }

}
