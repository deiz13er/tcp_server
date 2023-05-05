package com.example.tcp_server;

import com.example.ISO8583.entities.ISOMessage;
import com.example.ISO8583.enums.FIELDS;
import com.example.ISO8583.enums.MESSAGE_FUNCTION;
import com.example.ISO8583.enums.MESSAGE_ORIGIN;
import com.example.ISO8583.enums.VERSION;
import com.example.ISO8583.exceptions.ISOException;
import com.example.ISO8583.utils.FixedBitSet;
import com.example.ISO8583.utils.StringUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import com.example.ISO8583.builders.ISOMessageBuilder;

import java.nio.ByteBuffer;
import java.util.Arrays;


public class SimpleTCPChannelHandler extends SimpleChannelInboundHandler<byte[]> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Utils.log(ctx.channel().remoteAddress(), "Channel Active");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] s) throws Exception {
        byte[] bLen = {s[0], s[1]};
        byte[] msg = Arrays.copyOfRange(s,2, s.length);
        ISOMessage inputIsoMessage = ISOMessageBuilder.Unpacker()
                .setMessage(msg)
                .build();
        System.out.println(textIsoMessage(inputIsoMessage));

        ISOMessage outputIsoMessage = puckMessage(inputIsoMessage);


        ByteBuffer buffer = initBuffer(outputIsoMessage);
        ctx.channel().writeAndFlush(buffer.array());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Utils.log(ctx.channel().remoteAddress(), "Channel Inactive");
    }

    private static ISOMessage puckMessage(ISOMessage inputIsoMessage) throws ISOException {
        ISOMessage outputIsoMessage;
        if(inputIsoMessage.getMti().equals("0800") && StringUtil.fromByteArray(inputIsoMessage.getField(24)).equals("831F")) {
            outputIsoMessage = networkManagementResponse();
        } else if (inputIsoMessage.getMti().equals("0800") && StringUtil.fromByteArray(inputIsoMessage.getField(24)).equals("811F")) {
            outputIsoMessage = networkKeyChangeResponse();
        } else if (inputIsoMessage.getMti().equals("0800") && StringUtil.fromByteArray(inputIsoMessage.getField(24)).equals("815F")) {
            outputIsoMessage = networkMACKeyChangeResponse();
        } else if (inputIsoMessage.getMti().equals("0200") && StringUtil.fromByteArray(inputIsoMessage.getField(24)).equals("200F")
            && StringUtil.fromByteArray(inputIsoMessage.getField(3)).startsWith("00") && StringUtil.fromByteArray(inputIsoMessage.getField(3)).endsWith("000")) {
            outputIsoMessage = purchaseResponse();
        } else if (inputIsoMessage.getMti().equals("0200") && StringUtil.fromByteArray(inputIsoMessage.getField(24)).equals("200F")
                && StringUtil.fromByteArray(inputIsoMessage.getField(3)).startsWith("20") && StringUtil.fromByteArray(inputIsoMessage.getField(3)).endsWith("000")) {
            outputIsoMessage = returnRefundResponse();
        } else if (inputIsoMessage.getMti().equals("0400") && StringUtil.fromByteArray(inputIsoMessage.getField(24)).equals("400F")
                && StringUtil.fromByteArray(inputIsoMessage.getField(3)).startsWith("00") && StringUtil.fromByteArray(inputIsoMessage.getField(3)).endsWith("000")) {
            outputIsoMessage = purchaseReversalResponse();
        } else if (inputIsoMessage.getMti().equals("0400") && StringUtil.fromByteArray(inputIsoMessage.getField(24)).equals("400F")
                && StringUtil.fromByteArray(inputIsoMessage.getField(3)).startsWith("20") && StringUtil.fromByteArray(inputIsoMessage.getField(3)).endsWith("000")) {
            outputIsoMessage = returnReversalResponse();
        }

        else {
            outputIsoMessage = defaultResponse();
        }

        return outputIsoMessage;
    }
    private ByteBuffer initBuffer(ISOMessage isoMessage) {
        int len = isoMessage.getBody().length + isoMessage.getHeader().length;
        int length = 2;
        ByteBuffer buffer = ByteBuffer.allocate(len + length);

        if(length > 0)
        {
            byte[] mlen = ByteBuffer.allocate(4).putInt(len).array();
            buffer.put(Arrays.copyOfRange(mlen, 2,4));
        }

        buffer.put(isoMessage.getHeader())
                .put(isoMessage.getBody());

        return buffer;
    }

    private static ISOMessage returnRefundResponse() throws ISOException {
        return ISOMessageBuilder.Packer(VERSION.V1987)
                .financial()
                .mti(MESSAGE_FUNCTION.RequestResponse, MESSAGE_ORIGIN.Acquirer)
                .processCode("200000")
                .setField(FIELDS.F2_PAN, "54233900074112640123")
                .setField(FIELDS.F4_AmountTransaction, "000000000100")
                .setField(FIELDS.F7_TransmissionDateTime, "0706102042")
                .setField(FIELDS.F11_STAN, "000001")
                .setField(FIELDS.F12_LocalTime, "102034")
                .setField(FIELDS.F13_LocalDate, "0706")
                .setField(FIELDS.F37_RRN, "000001007050")
                .setField(FIELDS.F38_AuthIdResponse, "000000")
                .setField(FIELDS.F39_ResponseCode, "000")
                .setField(FIELDS.F41_CA_TerminalID, "00010001")
                .setField(FIELDS.F46_AddData_ISO, "00840D0000000061000000D00000000840")
                .setField(FIELDS.F49_CurrencyCode_Transaction, "810")
                .setField(FIELDS.F55_ICC, "b255")
                .setField(FIELDS.F64_MAC, "0102030405060708")
                .setHeader("3038303082")
                .build();
    }

    private static ISOMessage purchaseResponse() throws ISOException {
        return ISOMessageBuilder.Packer(VERSION.V1987)
                .financial()
                .mti(MESSAGE_FUNCTION.RequestResponse, MESSAGE_ORIGIN.Acquirer)
                .processCode("000000")
                .setField(FIELDS.F2_PAN, "54233900074112640123")
                .setField(FIELDS.F4_AmountTransaction, "000000000100")
                .setField(FIELDS.F7_TransmissionDateTime, "0706102042")
                .setField(FIELDS.F11_STAN, "000001")
                .setField(FIELDS.F12_LocalTime, "102034")
                .setField(FIELDS.F13_LocalDate, "0706")
                .setField(FIELDS.F37_RRN, "000001007050")
                .setField(FIELDS.F38_AuthIdResponse, "000000")
                .setField(FIELDS.F39_ResponseCode, "000")
                .setField(FIELDS.F41_CA_TerminalID, "00010001")
                .setField(FIELDS.F46_AddData_ISO, "00840D0000000061000000D00000000840")
                .setField(FIELDS.F48_AddData_Private, "0140011014001M")
                .setField(FIELDS.F49_CurrencyCode_Transaction, "810")
                .setField(FIELDS.F55_ICC, "b255")
                .setField(FIELDS.F62_Reserved_Private, "Call Issuer")
                .setField(FIELDS.F64_MAC, "0102030405060708")
                .setHeader("3038303082")
                .build();
    }

    private static ISOMessage networkManagementResponse() throws ISOException {
        return ISOMessageBuilder.Packer(VERSION.V1987)
                .networkManagement()
                .mti(MESSAGE_FUNCTION.RequestResponse, MESSAGE_ORIGIN.Acquirer)
                .processCode("990000")
                .setField(FIELDS.F7_TransmissionDateTime, "0706102034")
                .setField(FIELDS.F11_STAN, "1")
                .setField(FIELDS.F37_RRN, "000001007050")
                .setField(FIELDS.F39_ResponseCode, "000")
                .setField(FIELDS.F41_CA_TerminalID, "00010001")
                .setHeader("3038303082")
                .build();
    }

    private static ISOMessage networkKeyChangeResponse() throws ISOException {
        return ISOMessageBuilder.Packer(VERSION.V1987)
                .networkManagement()
                .mti(MESSAGE_FUNCTION.RequestResponse, MESSAGE_ORIGIN.Acquirer)
                .processCode("990000")
                .setField(FIELDS.F7_TransmissionDateTime, "0706102034")
                .setField(FIELDS.F11_STAN, "1")
                .setField(FIELDS.F37_RRN, "000001007050")
                .setField(FIELDS.F39_ResponseCode, "000")
                .setField(FIELDS.F41_CA_TerminalID, "00010001")
                .setField(FIELDS.F53_SecurityControlInfo, "3E45B5667F2AFFB3")
                .setField(FIELDS.F64_MAC, "0001020304050607")
                .setHeader("3038303082")
                .build();
    }

    private static ISOMessage networkMACKeyChangeResponse() throws ISOException {
        return ISOMessageBuilder.Packer(VERSION.V1987)
                .networkManagement()
                .mti(MESSAGE_FUNCTION.RequestResponse, MESSAGE_ORIGIN.Acquirer)
                .processCode("990000")
                .setField(FIELDS.F7_TransmissionDateTime, "0706102034")
                .setField(FIELDS.F11_STAN, "1")
                .setField(FIELDS.F37_RRN, "000001007050")
                .setField(FIELDS.F39_ResponseCode, "000")
                .setField(FIELDS.F41_CA_TerminalID, "00010001")
                .setField(FIELDS.F53_SecurityControlInfo, "3E45B5667F2AFFB3")
                .setHeader("3038303082")
                .build();
    }
    private static ISOMessage purchaseReversalResponse() throws ISOException {
        return ISOMessageBuilder.Packer(VERSION.V1987)
                .reversal()
                .mti(MESSAGE_FUNCTION.RequestResponse, MESSAGE_ORIGIN.Acquirer)
                .processCode("002000")
                .setField(FIELDS.F2_PAN, "54233900074112640123")
                .setField(FIELDS.F4_AmountTransaction, "000000000100")
                .setField(FIELDS.F7_TransmissionDateTime, "0706102042")
                .setField(FIELDS.F11_STAN, "000001")
                .setField(FIELDS.F12_LocalTime, "102034")
                .setField(FIELDS.F13_LocalDate, "0706")
                .setField(FIELDS.F37_RRN, "000001007050")
                .setField(FIELDS.F38_AuthIdResponse, "000000")
                .setField(FIELDS.F39_ResponseCode, "000")
                .setField(FIELDS.F41_CA_TerminalID, "00010001")
                .setField(FIELDS.F49_CurrencyCode_Transaction, "810")
                .setField(FIELDS.F55_ICC, "b255")
                .setField(FIELDS.F64_MAC, "0102030405060708")
                .setHeader("3038303082")
                .build();
    }

    private static ISOMessage returnReversalResponse() throws ISOException {
        return ISOMessageBuilder.Packer(VERSION.V1987)
                .reversal()
                .mti(MESSAGE_FUNCTION.RequestResponse, MESSAGE_ORIGIN.Acquirer)
                .processCode("002000")
                .setField(FIELDS.F2_PAN, "54233900074112640123")
                .setField(FIELDS.F4_AmountTransaction, "000000000100")
                .setField(FIELDS.F7_TransmissionDateTime, "0706102042")
                .setField(FIELDS.F11_STAN, "000001")
                .setField(FIELDS.F12_LocalTime, "102034")
                .setField(FIELDS.F13_LocalDate, "0706")
                .setField(FIELDS.F37_RRN, "000001007050")
                .setField(FIELDS.F38_AuthIdResponse, "000000")
                .setField(FIELDS.F39_ResponseCode, "000")
                .setField(FIELDS.F41_CA_TerminalID, "00010001")
                .setField(FIELDS.F49_CurrencyCode_Transaction, "810")
                .setField(FIELDS.F55_ICC, "b255")
                .setField(FIELDS.F64_MAC, "0102030405060708")
                .setHeader("3038303082")
                .build();
    }

    private static ISOMessage defaultResponse() throws ISOException {
        return ISOMessageBuilder.Packer(VERSION.V1987)
                .networkManagement()
                .mti(MESSAGE_FUNCTION.RequestResponse, MESSAGE_ORIGIN.Acquirer)
                .processCode("990000")
                .setHeader("3038303082")
                .build();
    }

    private static StringBuilder textIsoMessage(ISOMessage isoMessage) {
        StringBuilder message_text = new StringBuilder();
        FixedBitSet pb = new FixedBitSet(64);
        pb.fromHexString(StringUtil.fromByteArray(isoMessage.getPrimaryBitmap()));
        int offset = 10;
        message_text.append("MTI: "+isoMessage.getMti()+"\n");
        message_text.append("Primary bitmap: "+StringUtil.fromByteArray(isoMessage.getPrimaryBitmap())+"\n");
        for (int o : pb.getIndexes()) {

            FIELDS field = FIELDS.valueOf(o);

            if (field.isFixed()) {
                int len = field.getLength();
                switch (field.getType()) {
                    case "n":
                        if (len % 2 != 0)
                            len++;
                        len = len / 2;
                        message_text.append(field.toString() +": "+ StringUtil.fromByteArray(Arrays.copyOfRange(isoMessage.getBody(), offset, offset + len))+"\n");
                        break;
                    default:
                        message_text.append(field.toString() +": "+ StringUtil.asciiFromByteArray(Arrays.copyOfRange(isoMessage.getBody(), offset, offset + len))+"\n");
                        break;
                }
                offset += len;
            } else {

                int formatLength = 1;
                switch (field.getFormat()) {
                    case "LL":
                        formatLength = 1;
                        break;
                    case "LLL":
                        formatLength = 2;
                        break;
                }

                int flen = Integer.valueOf(StringUtil.fromByteArray(Arrays.copyOfRange(isoMessage.getBody(), offset, offset + formatLength)));
                offset = offset + formatLength;
                switch (field.getType()) {
                    case "z":
                        message_text.append(field.toString() +": "+ StringUtil.asciiFromByteArray(Arrays.copyOfRange(isoMessage.getBody(), offset, offset + flen))+"\n");
                        break;
                    case "n":
                        if (flen % 2 != 0)
                            flen++;
                        flen /= 2;
                        message_text.append(field.toString() +": "+ StringUtil.fromByteArray(Arrays.copyOfRange(isoMessage.getBody(), offset, offset + flen))+"\n");
                        break;
                    default:
                        message_text.append(field.toString() +": "+ StringUtil.asciiFromByteArray(Arrays.copyOfRange(isoMessage.getBody(), offset, offset + flen))+"\n");
                        break;
                }



                offset += flen;
            }

        }
        return message_text;
    }
}