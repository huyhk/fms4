package com.megatech.fms.helpers;


import java.util.HashMap;
import java.util.Hashtable;

public class UnicodeMap {
    public UnicodeMap(char unicodeChar, char replaceChar, char[] definedArray)
    {
        this.unicodeChar = unicodeChar;
        this.replaceChar = replaceChar;
        this.definedArray = definedArray;
    }
    private char unicodeChar;
    private char replaceChar;
    private char[] definedArray;

    public char getUnicodeChar() {
        return unicodeChar;
    }

    public void setUnicodeChar(char unicodeChar) {
        this.unicodeChar = unicodeChar;
    }

    public char getReplaceChar() {
        return replaceChar;
    }

    public void setReplaceChar(char replaceChar) {
        this.replaceChar = replaceChar;
    }

    public char[] getDefinedArray() {
        return definedArray;
    }

    public void setDefinedArray(char[] definedArray) {
        this.definedArray = definedArray;
    }

    public static Hashtable<Character,UnicodeMap> hashtable;

    public static Hashtable<Character, UnicodeMap> getHashtable() {
        if (hashtable == null) {
            hashtable = new Hashtable<>();
            hashtable.put('à', new UnicodeMap('à',(char)126,new char[]{0x07, 0x00, 0x08, 0x80, 0xA0, 0x00, 0x08, 0x80, 0x60, 0x00, 0x08, 0x80, 0x20, 0x00, 0x1F, 0x00, 0x00, 0x80}));
            hashtable.put('ả', new UnicodeMap('ả',(char)126,new char[]{0x07, 0x00, 0x08, 0x80, 0x20, 0x00, 0x88, 0x80, 0x20, 0x00, 0xC8, 0x80, 0x20, 0x00, 0x1F, 0x00, 0x00, 0x80}));
            hashtable.put('ã', new UnicodeMap('ã',(char)126,new char[]{0x07, 0x00, 0x48, 0x80, 0x20, 0x00, 0x88, 0x80, 0x20, 0x00, 0x48, 0x80, 0x20, 0x00, 0x9F, 0x00, 0x00, 0x80}));
            hashtable.put('á', new UnicodeMap('á',(char)126,new char[]{0x07, 0x00, 0x08, 0x80, 0x20, 0x00, 0x08, 0x80, 0x60, 0x00, 0x08, 0x80, 0xA0, 0x00, 0x1F, 0x00, 0x00, 0x80}));
            hashtable.put('ạ', new UnicodeMap('ạ',(char)126,new char[]{0x0E, 0x00, 0x11, 0x00, 0x40, 0x00, 0x11, 0x00, 0x40, 0x80, 0x11, 0x00, 0x40, 0x00, 0x3E, 0x00, 0x01, 0x00}));

            hashtable.put('ă', new UnicodeMap('ă',(char)126,new char[]{0x07, 0x00, 0x88, 0x80, 0x20, 0x00, 0x48, 0x80, 0x20, 0x00, 0x48, 0x80, 0x20, 0x00, 0x9F, 0x00, 0x00, 0x80}));
            hashtable.put('ằ', new UnicodeMap('ằ',(char)126,new char[]{0x03, 0x00, 0x44, 0x80, 0x10, 0x00, 0xA4, 0x80, 0x10, 0x00, 0x64, 0x80, 0x10, 0x00, 0x4F, 0x00, 0x00, 0x80}));
            hashtable.put('ẳ', new UnicodeMap('ẳ',(char)126,new char[]{0x03, 0x00, 0x44, 0x80, 0x10, 0x00, 0xA4, 0x80, 0x10, 0x00, 0xE4, 0x80, 0x10, 0x00, 0x4F, 0x00, 0x00, 0x80}));
            hashtable.put('ẵ', new UnicodeMap('ẵ',(char)126,new char[]{0x03, 0x00, 0x44, 0x80, 0x10, 0x00, 0x64, 0x80, 0x90, 0x00, 0x64, 0x80, 0x90, 0x00, 0x4F, 0x00, 0x00, 0x80}));
            hashtable.put('ắ', new UnicodeMap('ắ',(char)126,new char[]{0x03, 0x00, 0x44, 0x80, 0x10, 0x00, 0x64, 0x80, 0x10, 0x00, 0xA4, 0x80, 0x10, 0x00, 0x4F, 0x00, 0x00, 0x80}));
            hashtable.put('ặ', new UnicodeMap('ặ',(char)126,new char[]{0x06, 0x00, 0x89, 0x00, 0x20, 0x00, 0x49, 0x00, 0x20, 0x80, 0x49, 0x00, 0x20, 0x00, 0x9E, 0x00, 0x01, 0x00}));

            hashtable.put('â', new UnicodeMap('â',(char)126,new char[]{0x07, 0x00, 0x48, 0x80, 0x20, 0x00, 0x88, 0x80, 0x20, 0x00, 0x88, 0x80, 0x20, 0x00, 0x5F, 0x00, 0x00, 0x80}));
            hashtable.put('ầ', new UnicodeMap('ầ',(char)126,new char[]{0x03, 0x00, 0xA4, 0x80, 0x10, 0x00, 0x44, 0x80, 0x10, 0x00, 0x44, 0x80, 0x10, 0x00, 0x2F, 0x00, 0x00, 0x80}));
            hashtable.put('ẩ', new UnicodeMap('ẩ',(char)126,new char[]{0x03, 0x00, 0x24, 0x80, 0x10, 0x00, 0x44, 0x80, 0x10, 0x00, 0xC4, 0x80, 0x10, 0x00, 0xEF, 0x00, 0x00, 0x80}));
            hashtable.put('ẫ', new UnicodeMap('ẫ',(char)126,new char[]{0x03, 0x00, 0x64, 0x80, 0x10, 0x00, 0xC4, 0x80, 0x10, 0x00, 0x44, 0x80, 0x10, 0x00, 0xAF, 0x00, 0x00, 0x80}));
            hashtable.put('ấ', new UnicodeMap('ấ',(char)126,new char[]{0x03, 0x00, 0xA4, 0x80, 0x10, 0x00, 0x44, 0x80, 0x10, 0x00, 0x44, 0x80, 0x10, 0x00, 0x2F, 0x00, 0x00, 0x80}));
            hashtable.put('ậ', new UnicodeMap('ậ',(char)126,new char[]{0x06, 0x00, 0x49, 0x00, 0x20, 0x00, 0x89, 0x00, 0x20, 0x80, 0x89, 0x00, 0x20, 0x00, 0x5E, 0x00, 0x01, 0x00}));

            hashtable.put('è', new UnicodeMap('è',(char)126,new char[]{0x0F, 0x00, 0x10, 0x80, 0x84, 0x00, 0x10, 0x80, 0x44, 0x00, 0x10, 0x80, 0x04, 0x00, 0x10, 0x80, 0x0D, 0x00}));
            hashtable.put('ẻ', new UnicodeMap('ẻ',(char)126,new char[]{0x0F, 0x00, 0x10, 0x80, 0x04, 0x00, 0x90, 0x80, 0x04, 0x00, 0xD0, 0x80, 0x04, 0x00, 0x10, 0x80, 0x0D, 0x00}));
            hashtable.put('ẽ', new UnicodeMap('ẽ',(char)126,new char[]{0x0F, 0x00, 0x50, 0x80, 0x04, 0x00, 0x90, 0x80, 0x04, 0x00, 0x50, 0x80, 0x04, 0x00, 0x90, 0x80, 0x0D, 0x00}));
            hashtable.put('é', new UnicodeMap('é',(char)126,new char[]{0x0F, 0x00, 0x10, 0x80, 0x04, 0x00, 0x10, 0x80, 0x44, 0x00, 0x10, 0x80, 0x84, 0x00, 0x10, 0x80, 0x0D, 0x00}));
            hashtable.put('ẹ', new UnicodeMap('ẹ',(char)126,new char[]{0x1E, 0x00, 0x21, 0x00, 0x08, 0x00, 0x21, 0x00, 0x08, 0x80, 0x21, 0x00, 0x08, 0x00, 0x21, 0x00, 0x1A, 0x00}));

            hashtable.put('ê', new UnicodeMap('ê',(char)126,new char[]{0x0F, 0x00, 0x50, 0x80, 0x04, 0x00, 0x90, 0x80, 0x04, 0x00, 0x90, 0x80, 0x04, 0x00, 0x50, 0x80, 0x0D, 0x00}));
            hashtable.put('ề', new UnicodeMap('ề',(char)126,new char[]{0x0F, 0x00, 0xB0, 0x80, 0x04, 0x00, 0x50, 0x80, 0x04, 0x00, 0x50, 0x80, 0x04, 0x00, 0x30, 0x80, 0x0D, 0x00}));
            hashtable.put('ể', new UnicodeMap('ể',(char)126,new char[]{0x0F, 0x00, 0x30, 0x80, 0x04, 0x00, 0x50, 0x80, 0x04, 0x00, 0x50, 0x80, 0x84, 0x00, 0x30, 0x80, 0xCD, 0x00}));
            hashtable.put('ễ', new UnicodeMap('ễ',(char)126,new char[]{0x0F, 0x00, 0x70, 0x80, 0x04, 0x00, 0xD0, 0x80, 0x04, 0x00, 0x50, 0x80, 0x04, 0x00, 0xB0, 0x80, 0x0D, 0x00}));
            hashtable.put('ế', new UnicodeMap('ế',(char)126,new char[]{0x0F, 0x00, 0x30, 0x80, 0x04, 0x00, 0x50, 0x80, 0x04, 0x00, 0x50, 0x80, 0x04, 0x00, 0xB0, 0x80, 0x0D, 0x00}));
            hashtable.put('ệ', new UnicodeMap('ệ',(char)126,new char[]{0x1E, 0x00, 0x61, 0x00, 0x08, 0x00, 0xA1, 0x00, 0x08, 0x80, 0xA1, 0x00, 0x08, 0x00, 0x61, 0x00, 0x1A, 0x00}));


            //hashtable.put('o', new UnicodeMap('o',(char)126,new char[]{0x0F, 0x00, 0x10, 0x80, 0x00, 0x00, 0x10, 0x80, 0x00, 0x00, 0x10, 0x80, 0x00, 0x00, 0x10, 0x80, 0x0F, 0x00}));
            hashtable.put('ò', new UnicodeMap('ò',(char)126,new char[]{0x0F, 0x00, 0x10, 0x80, 0x80, 0x00, 0x10, 0x80, 0x40, 0x00, 0x10, 0x80, 0x00, 0x00, 0x10, 0x80, 0x0F, 0x00}));
            hashtable.put('ỏ', new UnicodeMap('ỏ',(char)126,new char[]{0x0F, 0x00, 0x10, 0x80, 0x00, 0x00, 0x90, 0x80, 0x00, 0x00, 0xD0, 0x80, 0x00, 0x00, 0x10, 0x80, 0x0F, 0x00}));
            hashtable.put('õ', new UnicodeMap('õ',(char)126,new char[]{0x0F, 0x00, 0x50, 0x80, 0x00, 0x00, 0x90, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0x90, 0x80, 0x0F, 0x00}));
            hashtable.put('ó', new UnicodeMap('ó',(char)126,new char[]{0x0F, 0x00, 0x10, 0x80, 0x00, 0x00, 0x10, 0x80, 0x40, 0x00, 0x10, 0x80, 0x80, 0x00, 0x10, 0x80, 0x0F, 0x00}));
            hashtable.put('ọ', new UnicodeMap('ọ',(char)126,new char[]{0x1E, 0x00, 0x21, 0x00, 0x00, 0x00, 0x21, 0x00, 0x00, 0x80, 0x21, 0x00, 0x00, 0x00, 0x21, 0x00, 0x1E, 0x00}));

            hashtable.put('ô', new UnicodeMap('ô',(char)126,new char[]{0x0F, 0x00, 0x30, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0x30, 0x80, 0x0F, 0x00}));
            hashtable.put('ồ', new UnicodeMap('ồ',(char)126,new char[]{0x0F, 0x00, 0xB0, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0x30, 0x80, 0x0F, 0x00}));
            hashtable.put('ổ', new UnicodeMap('ổ',(char)126,new char[]{0x0F, 0x00, 0x30, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0x50, 0x80, 0x80, 0x00, 0x30, 0x80, 0xCF, 0x00}));
            hashtable.put('ỗ', new UnicodeMap('ỗ',(char)126,new char[]{0x0F, 0x00, 0x70, 0x80, 0x00, 0x00, 0xD0, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0xB0, 0x80, 0x0F, 0x00}));
            hashtable.put('ố', new UnicodeMap('ố',(char)126,new char[]{0x0F, 0x00, 0x30, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0xB0, 0x80, 0x0F, 0x00}));
            hashtable.put('ộ', new UnicodeMap('ộ',(char)126,new char[]{0x1E, 0x00, 0x61, 0x00, 0x00, 0x00, 0xA1, 0x00, 0x00, 0x80, 0xA1, 0x00, 0x00, 0x00, 0x61, 0x00, 0x1E, 0x00}));

            hashtable.put('ơ', new UnicodeMap('ơ',(char)126,new char[]{0x0F, 0x00, 0x10, 0x80, 0x00, 0x00, 0x10, 0x80, 0x00, 0x00, 0x10, 0x80, 0x40, 0x00, 0x10, 0x80, 0x6F, 0x00}));
            hashtable.put('ờ', new UnicodeMap('ờ',(char)126,new char[]{0x0F, 0x00, 0x90, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0x10, 0x80, 0x40, 0x00, 0x10, 0x80, 0x6F, 0x00}));
            hashtable.put('ở', new UnicodeMap('ở',(char)126,new char[]{0x0F, 0x00, 0x90, 0x80, 0x00, 0x00, 0xD0, 0x80, 0x00, 0x00, 0x10, 0x80, 0x40, 0x00, 0x10, 0x80, 0x6F, 0x00}));
            hashtable.put('ỡ', new UnicodeMap('ỡ',(char)126,new char[]{0x4F, 0x00, 0x10, 0x80, 0x80, 0x00, 0x10, 0x80, 0x40, 0x00, 0x10, 0x80, 0xC0, 0x00, 0x10, 0x80, 0x6F, 0x00}));
            hashtable.put('ớ', new UnicodeMap('ớ',(char)126,new char[]{0x0F, 0x00, 0x50, 0x80, 0x00, 0x00, 0x90, 0x80, 0x00, 0x00, 0x10, 0x80, 0x40, 0x00, 0x10, 0x80, 0x6F, 0x00}));
            hashtable.put('ợ', new UnicodeMap('ợ',(char)126,new char[]{0x1E, 0x00, 0x21, 0x00, 0x00, 0x00, 0x21, 0x00, 0x00, 0x80, 0x21, 0x00, 0x80, 0x00, 0x21, 0x00, 0xDE, 0x00}));

            //hashtable.put('u', new UnicodeMap('u',(char)126,new char[]{0x3F, 0x00, 0x00, 0x80, 0x00, 0x00, 0x00, 0x80, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x3F, 0x00, 0x00, 0x80}));
            hashtable.put('ù', new UnicodeMap('ù',(char)126,new char[]{0x3F, 0x00, 0x00, 0x80, 0x80, 0x00, 0x00, 0x80, 0x40, 0x00, 0x01, 0x00, 0x00, 0x00, 0x3F, 0x00, 0x00, 0x80}));
            hashtable.put('ủ', new UnicodeMap('ủ',(char)126,new char[]{0x3F, 0x00, 0x00, 0x80, 0x80, 0x00, 0x00, 0x80, 0xC0, 0x00, 0x01, 0x00, 0x00, 0x00, 0x3F, 0x00, 0x00, 0x80}));
            hashtable.put('ũ', new UnicodeMap('ũ',(char)126,new char[]{0x3F, 0x00, 0x40, 0x80, 0x00, 0x00, 0x80, 0x80, 0x00, 0x00, 0x41, 0x00, 0x00, 0x00, 0xBF, 0x00, 0x00, 0x80}));
            hashtable.put('ú', new UnicodeMap('ú',(char)126,new char[]{0x3F, 0x00, 0x00, 0x80, 0x40, 0x00, 0x00, 0x80, 0x80, 0x00, 0x01, 0x00, 0x00, 0x00, 0x3F, 0x00, 0x00, 0x80}));
            hashtable.put('ụ', new UnicodeMap('ụ',(char)126,new char[]{0x7E, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x80, 0x02, 0x00, 0x00, 0x00, 0x7F, 0x00, 0x00, 0x00}));

            hashtable.put('ư', new UnicodeMap('ư',(char)126,new char[]{0x3F, 0x00, 0x00, 0x80, 0x00, 0x00, 0x00, 0x80, 0x00, 0x00, 0x01, 0x00, 0x40, 0x00, 0x3F, 0x00, 0x60, 0x80}));
            hashtable.put('ừ', new UnicodeMap('ừ',(char)126,new char[]{0x3F, 0x00, 0x80, 0x80, 0x00, 0x00, 0x40, 0x80, 0x00, 0x00, 0x01, 0x00, 0x40, 0x00, 0x3F, 0x00, 0x60, 0x80}));
            hashtable.put('ử', new UnicodeMap('ử',(char)126,new char[]{0x3F, 0x00, 0x80, 0x80, 0x00, 0x00, 0xC0, 0x80, 0x00, 0x00, 0x01, 0x00, 0x40, 0x00, 0x3F, 0x00, 0x60, 0x80}));
            hashtable.put('ữ', new UnicodeMap('ữ',(char)126,new char[]{0x7F, 0x00, 0x00, 0x80, 0x80, 0x00, 0x00, 0x80, 0x40, 0x00, 0x01, 0x00, 0xC0, 0x00, 0x3F, 0x00, 0x60, 0x80}));
            hashtable.put('ứ', new UnicodeMap('ứ',(char)126,new char[]{0x3F, 0x00, 0x00, 0x80, 0x40, 0x00, 0x00, 0x80, 0x80, 0x00, 0x01, 0x00, 0x40, 0x00, 0x3F, 0x00, 0x60, 0x80}));
            hashtable.put('ự', new UnicodeMap('ự',(char)126,new char[]{0x7E, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x80, 0x02, 0x00, 0x80, 0x00, 0x3F, 0x00, 0xC0, 0x00}));

            hashtable.put('đ', new UnicodeMap('đ',(char)126,new char[]{0x0F, 0x00, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0xFF, 0x80, 0x00, 0x00, 0x40, 0x00}));
            hashtable.put('Đ', new UnicodeMap('Đ',(char)126,new char[]{0x88, 0x80, 0x08, 0x00, 0xF7, 0x80, 0x08, 0x00, 0x80, 0x80, 0x08, 0x00, 0x80, 0x80, 0x41, 0x00, 0x3E, 0x00}));

            //hashtable.put('i', new UnicodeMap('i',(char)126,new char[]{0x00, 0x00, 0x00, 0x80, 0x10, 0x00, 0x00, 0x80, 0x5F, 0x00, 0x00, 0x80, 0x00, 0x00, 0x00, 0x80, 0x00, 0x00}));
            hashtable.put('ì', new UnicodeMap('ì',(char)126,new char[]{0x00, 0x00, 0x00, 0x80, 0x90, 0x00, 0x00, 0x80, 0x5F, 0x00, 0x00, 0x80, 0x00, 0x00, 0x00, 0x80, 0x00, 0x00}));
            hashtable.put('ỉ', new UnicodeMap('ỉ',(char)126,new char[]{0x00, 0x00, 0x00, 0x80, 0x90, 0x00, 0x00, 0x80, 0xDF, 0x00, 0x00, 0x80, 0x00, 0x00, 0x00, 0x80, 0x00, 0x00}));
            hashtable.put('ĩ', new UnicodeMap('ĩ',(char)126,new char[]{0x00, 0x00, 0x40, 0x80, 0x10, 0x00, 0x80, 0x80, 0x5F, 0x00, 0x40, 0x80, 0x00, 0x00, 0x80, 0x80, 0x00, 0x00}));
            hashtable.put('í', new UnicodeMap('í',(char)126,new char[]{0x00, 0x00, 0x00, 0x80, 0x10, 0x00, 0x00, 0x80, 0x5F, 0x00, 0x00, 0x80, 0x80, 0x00, 0x00, 0x80, 0x00, 0x00}));
            hashtable.put('ị', new UnicodeMap('ị',(char)126,new char[]{0x00, 0x00, 0x01, 0x00, 0x20, 0x00, 0x01, 0x00, 0xBE, 0x80, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00}));

            //hashtable.put('y', new UnicodeMap('y',(char)126,new char[]{0x20, 0x80, 0x10, 0x00, 0x08, 0x00, 0x05, 0x00, 0x02, 0x00, 0x04, 0x00, 0x08, 0x00, 0x10, 0x00, 0x20, 0x00}));
            hashtable.put('ỳ', new UnicodeMap('ỳ',(char)126,new char[]{0x20, 0x80, 0x10, 0x00, 0x88, 0x00, 0x05, 0x00, 0x42, 0x00, 0x04, 0x00, 0x08, 0x00, 0x10, 0x00, 0x20, 0x00}));
            hashtable.put('ỷ', new UnicodeMap('ỷ',(char)126,new char[]{0x20, 0x80, 0x10, 0x00, 0x08, 0x00, 0x85, 0x00, 0x02, 0x00, 0xC4, 0x00, 0x08, 0x00, 0x10, 0x00, 0x20, 0x00}));
            hashtable.put('ỹ', new UnicodeMap('ỹ',(char)126,new char[]{0x20, 0x80, 0x50, 0x00, 0x08, 0x00, 0x85, 0x00, 0x02, 0x00, 0x44, 0x00, 0x08, 0x00, 0x90, 0x00, 0x20, 0x00}));
            hashtable.put('ý', new UnicodeMap('ý',(char)126,new char[]{0x20, 0x80, 0x10, 0x00, 0x08, 0x00, 0x05, 0x00, 0x42, 0x00, 0x04, 0x00, 0x88, 0x00, 0x10, 0x00, 0x20, 0x00}));
            hashtable.put('ỵ', new UnicodeMap('ỵ',(char)126,new char[]{0x20, 0x80, 0x10, 0x00, 0x08, 0x00, 0x05, 0x00, 0x02, 0x00, 0x04, 0x80, 0x08, 0x00, 0x10, 0x00, 0x20, 0x00}));

            hashtable.put('À', new UnicodeMap('à',(char)126,new char[]{0x07, 0x00, 0x08, 0x80, 0xA0, 0x00, 0x08, 0x80, 0x60, 0x00, 0x08, 0x80, 0x20, 0x00, 0x1F, 0x00, 0x00, 0x80}));
            hashtable.put('Ả', new UnicodeMap('ả',(char)126,new char[]{0x07, 0x00, 0x08, 0x80, 0x20, 0x00, 0x88, 0x80, 0x20, 0x00, 0xC8, 0x80, 0x20, 0x00, 0x1F, 0x00, 0x00, 0x80}));
            hashtable.put('Ã', new UnicodeMap('ã',(char)126,new char[]{0x07, 0x00, 0x48, 0x80, 0x20, 0x00, 0x88, 0x80, 0x20, 0x00, 0x48, 0x80, 0x20, 0x00, 0x9F, 0x00, 0x40, 0x80}));
            hashtable.put('Á', new UnicodeMap('á',(char)126,new char[]{0x07, 0x00, 0x08, 0x80, 0x20, 0x00, 0x08, 0x80, 0x60, 0x00, 0x08, 0x80, 0xA0, 0x00, 0x1F, 0x00, 0x00, 0x80}));
            hashtable.put('Ạ', new UnicodeMap('ạ',(char)126,new char[]{0x0E, 0x00, 0x11, 0x00, 0x40, 0x00, 0x11, 0x00, 0x40, 0x80, 0x11, 0x00, 0x40, 0x00, 0x3E, 0x00, 0x01, 0x00}));

            hashtable.put('Ă', new UnicodeMap('ă',(char)126,new char[]{0x07, 0x00, 0x88, 0x80, 0x20, 0x00, 0x48, 0x80, 0x20, 0x00, 0x48, 0x80, 0x20, 0x00, 0x9F, 0x00, 0x00, 0x80}));
            hashtable.put('Ằ', new UnicodeMap('ằ',(char)126,new char[]{0x03, 0x00, 0x44, 0x80, 0x10, 0x00, 0xA4, 0x80, 0x10, 0x00, 0x64, 0x80, 0x10, 0x00, 0x4F, 0x00, 0x00, 0x80}));
            hashtable.put('Ẳ', new UnicodeMap('ẳ',(char)126,new char[]{0x03, 0x00, 0x44, 0x80, 0x10, 0x00, 0xA4, 0x80, 0x10, 0x00, 0xE4, 0x80, 0x10, 0x00, 0x4F, 0x00, 0x00, 0x80}));
            hashtable.put('Ẵ', new UnicodeMap('ẵ',(char)126,new char[]{0x03, 0x00, 0x44, 0x80, 0x10, 0x00, 0x64, 0x80, 0x90, 0x00, 0x64, 0x80, 0x90, 0x00, 0x4F, 0x00, 0x00, 0x80}));
            hashtable.put('Ắ', new UnicodeMap('ắ',(char)126,new char[]{0x03, 0x00, 0x44, 0x80, 0x10, 0x00, 0x64, 0x80, 0x10, 0x00, 0xA4, 0x80, 0x10, 0x00, 0x4F, 0x00, 0x00, 0x80}));
            hashtable.put('Ặ', new UnicodeMap('ặ',(char)126,new char[]{0x06, 0x00, 0x89, 0x00, 0x20, 0x00, 0x49, 0x00, 0x20, 0x80, 0x49, 0x00, 0x20, 0x00, 0x9E, 0x00, 0x01, 0x00}));

            hashtable.put('Â', new UnicodeMap('â',(char)126,new char[]{0x07, 0x00, 0x48, 0x80, 0x20, 0x00, 0x88, 0x80, 0x20, 0x00, 0x88, 0x80, 0x20, 0x00, 0x5F, 0x00, 0x00, 0x80}));
            hashtable.put('Ầ', new UnicodeMap('ầ',(char)126,new char[]{0x03, 0x00, 0xA4, 0x80, 0x10, 0x00, 0x44, 0x80, 0x10, 0x00, 0x44, 0x80, 0x10, 0x00, 0x2F, 0x00, 0x00, 0x80}));
            hashtable.put('Ẩ', new UnicodeMap('ẩ',(char)126,new char[]{0x03, 0x00, 0x24, 0x80, 0x10, 0x00, 0x44, 0x80, 0x10, 0x00, 0xC4, 0x80, 0x10, 0x00, 0xEF, 0x00, 0x00, 0x80}));
            hashtable.put('Ẫ', new UnicodeMap('ẫ',(char)126,new char[]{0x03, 0x00, 0x64, 0x80, 0x10, 0x00, 0xC4, 0x80, 0x10, 0x00, 0x44, 0x80, 0x10, 0x00, 0xAF, 0x00, 0x00, 0x80}));
            hashtable.put('Ấ', new UnicodeMap('ấ',(char)126,new char[]{0x03, 0x00, 0xA4, 0x80, 0x10, 0x00, 0x44, 0x80, 0x10, 0x00, 0x44, 0x80, 0x10, 0x00, 0x2F, 0x00, 0x00, 0x80}));
            hashtable.put('Ậ', new UnicodeMap('ậ',(char)126,new char[]{0x06, 0x00, 0x49, 0x00, 0x20, 0x00, 0x89, 0x00, 0x20, 0x80, 0x89, 0x00, 0x20, 0x00, 0x5E, 0x00, 0x01, 0x00}));

            hashtable.put('È', new UnicodeMap('è',(char)126,new char[]{0x0F, 0x00, 0x10, 0x80, 0x84, 0x00, 0x10, 0x80, 0x44, 0x00, 0x10, 0x80, 0x04, 0x00, 0x10, 0x80, 0x0D, 0x00}));
            hashtable.put('Ẻ', new UnicodeMap('ẻ',(char)126,new char[]{0x0F, 0x00, 0x10, 0x80, 0x04, 0x00, 0x90, 0x80, 0x04, 0x00, 0xD0, 0x80, 0x04, 0x00, 0x10, 0x80, 0x0D, 0x00}));
            hashtable.put('Ẽ', new UnicodeMap('ẽ',(char)126,new char[]{0x0F, 0x00, 0x50, 0x80, 0x04, 0x00, 0x90, 0x80, 0x04, 0x00, 0x50, 0x80, 0x04, 0x00, 0x90, 0x80, 0x0D, 0x00}));
            hashtable.put('É', new UnicodeMap('é',(char)126,new char[]{0x0F, 0x00, 0x10, 0x80, 0x04, 0x00, 0x10, 0x80, 0x44, 0x00, 0x10, 0x80, 0x84, 0x00, 0x10, 0x80, 0x0D, 0x00}));
            hashtable.put('Ẹ', new UnicodeMap('ẹ',(char)126,new char[]{0x1E, 0x00, 0x21, 0x00, 0x08, 0x00, 0x21, 0x00, 0x08, 0x80, 0x21, 0x00, 0x08, 0x00, 0x21, 0x00, 0x1A, 0x00}));

            hashtable.put('Ê', new UnicodeMap('ê',(char)126,new char[]{0x0F, 0x00, 0x50, 0x80, 0x04, 0x00, 0x90, 0x80, 0x04, 0x00, 0x90, 0x80, 0x04, 0x00, 0x50, 0x80, 0x0D, 0x00}));
            hashtable.put('Ề', new UnicodeMap('ề',(char)126,new char[]{0x0F, 0x00, 0xB0, 0x80, 0x04, 0x00, 0x50, 0x80, 0x04, 0x00, 0x50, 0x80, 0x04, 0x00, 0x30, 0x80, 0x0D, 0x00}));
            hashtable.put('Ể', new UnicodeMap('ể',(char)126,new char[]{0x0F, 0x00, 0x30, 0x80, 0x04, 0x00, 0x50, 0x80, 0x04, 0x00, 0x50, 0x80, 0x84, 0x00, 0x30, 0x80, 0xCD, 0x00}));
            hashtable.put('Ễ', new UnicodeMap('ễ',(char)126,new char[]{0x0F, 0x00, 0x70, 0x80, 0x04, 0x00, 0xD0, 0x80, 0x04, 0x00, 0x50, 0x80, 0x04, 0x00, 0xB0, 0x80, 0x0D, 0x00}));
            hashtable.put('Ế', new UnicodeMap('ế',(char)126,new char[]{0x0F, 0x00, 0x30, 0x80, 0x04, 0x00, 0x50, 0x80, 0x04, 0x00, 0x50, 0x80, 0x04, 0x00, 0xB0, 0x80, 0x0D, 0x00}));
            hashtable.put('Ệ', new UnicodeMap('ệ',(char)126,new char[]{0x1E, 0x00, 0x61, 0x00, 0x08, 0x00, 0xA1, 0x00, 0x08, 0x80, 0xA1, 0x00, 0x08, 0x00, 0x61, 0x00, 0x1A, 0x00}));

            //hashtable.put('O', new UnicodeMap('o',(char)126,new char[]{0x0F, 0x00, 0x10, 0x80, 0x00, 0x00, 0x10, 0x80, 0x00, 0x00, 0x10, 0x80, 0x00, 0x00, 0x10, 0x80, 0x0F, 0x00}));
            hashtable.put('Ò', new UnicodeMap('ò',(char)126,new char[]{0x0F, 0x00, 0x10, 0x80, 0x80, 0x00, 0x10, 0x80, 0x40, 0x00, 0x10, 0x80, 0x00, 0x00, 0x10, 0x80, 0x0F, 0x00}));
            hashtable.put('Ỏ', new UnicodeMap('ỏ',(char)126,new char[]{0x0F, 0x00, 0x10, 0x80, 0x00, 0x00, 0x90, 0x80, 0x00, 0x00, 0xD0, 0x80, 0x00, 0x00, 0x10, 0x80, 0x0F, 0x00}));
            hashtable.put('Õ', new UnicodeMap('õ',(char)126,new char[]{0x0F, 0x00, 0x50, 0x80, 0x00, 0x00, 0x90, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0x90, 0x80, 0x0F, 0x00}));
            hashtable.put('Ó', new UnicodeMap('ó',(char)126,new char[]{0x0F, 0x00, 0x10, 0x80, 0x00, 0x00, 0x10, 0x80, 0x40, 0x00, 0x10, 0x80, 0x80, 0x00, 0x10, 0x80, 0x0F, 0x00}));
            hashtable.put('Ọ', new UnicodeMap('ọ',(char)126,new char[]{0x1E, 0x00, 0x21, 0x00, 0x00, 0x00, 0x21, 0x00, 0x00, 0x80, 0x21, 0x00, 0x00, 0x00, 0x21, 0x00, 0x1E, 0x00}));

            hashtable.put('Ô', new UnicodeMap('ô',(char)126,new char[]{0x0F, 0x00, 0x30, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0x30, 0x80, 0x0F, 0x00}));
            hashtable.put('Ồ', new UnicodeMap('ồ',(char)126,new char[]{0x0F, 0x00, 0xB0, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0x30, 0x80, 0x0F, 0x00}));
            hashtable.put('Ổ', new UnicodeMap('ổ',(char)126,new char[]{0x0F, 0x00, 0x30, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0x50, 0x80, 0x80, 0x00, 0x30, 0x80, 0xCF, 0x00}));
            hashtable.put('Ỗ', new UnicodeMap('ỗ',(char)126,new char[]{0x0F, 0x00, 0x70, 0x80, 0x00, 0x00, 0xD0, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0xB0, 0x80, 0x0F, 0x00}));
            hashtable.put('Ố', new UnicodeMap('ố',(char)126,new char[]{0x0F, 0x00, 0x30, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0xB0, 0x80, 0x0F, 0x00}));
            hashtable.put('Ộ', new UnicodeMap('ộ',(char)126,new char[]{0x1E, 0x00, 0x61, 0x00, 0x00, 0x00, 0xA1, 0x00, 0x00, 0x80, 0xA1, 0x00, 0x00, 0x00, 0x61, 0x00, 0x1E, 0x00}));

            hashtable.put('Ơ', new UnicodeMap('ơ',(char)126,new char[]{0x0F, 0x00, 0x10, 0x80, 0x00, 0x00, 0x10, 0x80, 0x00, 0x00, 0x10, 0x80, 0x40, 0x00, 0x10, 0x80, 0x6F, 0x00}));
            hashtable.put('Ờ', new UnicodeMap('ờ',(char)126,new char[]{0x0F, 0x00, 0x90, 0x80, 0x00, 0x00, 0x50, 0x80, 0x00, 0x00, 0x10, 0x80, 0x40, 0x00, 0x10, 0x80, 0x6F, 0x00}));
            hashtable.put('Ở', new UnicodeMap('ở',(char)126,new char[]{0x0F, 0x00, 0x90, 0x80, 0x00, 0x00, 0xD0, 0x80, 0x00, 0x00, 0x10, 0x80, 0x40, 0x00, 0x10, 0x80, 0x6F, 0x00}));
            hashtable.put('Ỡ', new UnicodeMap('ỡ',(char)126,new char[]{0x4F, 0x00, 0x10, 0x80, 0x80, 0x00, 0x10, 0x80, 0x40, 0x00, 0x10, 0x80, 0xC0, 0x00, 0x10, 0x80, 0x6F, 0x00}));
            hashtable.put('Ớ', new UnicodeMap('ớ',(char)126,new char[]{0x0F, 0x00, 0x50, 0x80, 0x00, 0x00, 0x90, 0x80, 0x00, 0x00, 0x10, 0x80, 0x40, 0x00, 0x10, 0x80, 0x6F, 0x00}));
            hashtable.put('Ợ', new UnicodeMap('ợ',(char)126,new char[]{0x1E, 0x00, 0x21, 0x00, 0x00, 0x00, 0x21, 0x00, 0x00, 0x80, 0x21, 0x00, 0x80, 0x00, 0x21, 0x00, 0xDE, 0x00}));

            //hashtable.put('U', new UnicodeMap('u',(char)126,new char[]{0x3F, 0x00, 0x00, 0x80, 0x00, 0x00, 0x00, 0x80, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x3F, 0x00, 0x00, 0x80}));
            hashtable.put('Ù', new UnicodeMap('ù',(char)126,new char[]{0x3F, 0x00, 0x00, 0x80, 0x80, 0x00, 0x00, 0x80, 0x40, 0x00, 0x01, 0x00, 0x00, 0x00, 0x3F, 0x00, 0x00, 0x80}));
            hashtable.put('Ủ', new UnicodeMap('ủ',(char)126,new char[]{0x3F, 0x00, 0x00, 0x80, 0x80, 0x00, 0x00, 0x80, 0xC0, 0x00, 0x01, 0x00, 0x00, 0x00, 0x3F, 0x00, 0x00, 0x80}));
            hashtable.put('Ũ', new UnicodeMap('ũ',(char)126,new char[]{0x3F, 0x00, 0x40, 0x80, 0x00, 0x00, 0x80, 0x80, 0x00, 0x00, 0x41, 0x00, 0x00, 0x00, 0xBF, 0x00, 0x00, 0x80}));
            hashtable.put('Ú', new UnicodeMap('ú',(char)126,new char[]{0x3F, 0x00, 0x00, 0x80, 0x40, 0x00, 0x00, 0x80, 0x80, 0x00, 0x01, 0x00, 0x00, 0x00, 0x3F, 0x00, 0x00, 0x80}));
            hashtable.put('Ụ', new UnicodeMap('ụ',(char)126,new char[]{0x7E, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x80, 0x02, 0x00, 0x00, 0x00, 0x7F, 0x00, 0x00, 0x00}));

            hashtable.put('Ư', new UnicodeMap('ư',(char)126,new char[]{0x3F, 0x00, 0x00, 0x80, 0x00, 0x00, 0x00, 0x80, 0x00, 0x00, 0x01, 0x00, 0x40, 0x00, 0x3F, 0x00, 0x60, 0x80}));
            hashtable.put('Ừ', new UnicodeMap('ừ',(char)126,new char[]{0x3F, 0x00, 0x80, 0x80, 0x00, 0x00, 0x40, 0x80, 0x00, 0x00, 0x01, 0x00, 0x40, 0x00, 0x3F, 0x00, 0x60, 0x80}));
            hashtable.put('Ử', new UnicodeMap('ử',(char)126,new char[]{0x3F, 0x00, 0x80, 0x80, 0x00, 0x00, 0xC0, 0x80, 0x00, 0x00, 0x01, 0x00, 0x40, 0x00, 0x3F, 0x00, 0x60, 0x80}));
            hashtable.put('Ữ', new UnicodeMap('ữ',(char)126,new char[]{0x7F, 0x00, 0x00, 0x80, 0x80, 0x00, 0x00, 0x80, 0x40, 0x00, 0x01, 0x00, 0xC0, 0x00, 0x3F, 0x00, 0x60, 0x80}));
            hashtable.put('Ứ', new UnicodeMap('ứ',(char)126,new char[]{0x3F, 0x00, 0x00, 0x80, 0x40, 0x00, 0x00, 0x80, 0x80, 0x00, 0x01, 0x00, 0x40, 0x00, 0x3F, 0x00, 0x60, 0x80}));
            hashtable.put('Ự', new UnicodeMap('ự',(char)126,new char[]{0x7E, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x80, 0x02, 0x00, 0x80, 0x00, 0x3F, 0x00, 0xC0, 0x00}));

            //hashtable.put('I', new UnicodeMap('i',(char)126,new char[]{0x00, 0x00, 0x00, 0x80, 0x10, 0x00, 0x00, 0x80, 0x5F, 0x00, 0x00, 0x80, 0x00, 0x00, 0x00, 0x80, 0x00, 0x00}));
            hashtable.put('Ì', new UnicodeMap('ì',(char)126,new char[]{0x00, 0x00, 0x00, 0x80, 0x90, 0x00, 0x00, 0x80, 0x5F, 0x00, 0x00, 0x80, 0x00, 0x00, 0x00, 0x80, 0x00, 0x00}));
            hashtable.put('Ỉ', new UnicodeMap('ỉ',(char)126,new char[]{0x00, 0x00, 0x00, 0x80, 0x90, 0x00, 0x00, 0x80, 0xDF, 0x00, 0x00, 0x80, 0x00, 0x00, 0x00, 0x80, 0x00, 0x00}));
            hashtable.put('Ĩ', new UnicodeMap('ĩ',(char)126,new char[]{0x00, 0x00, 0x40, 0x80, 0x10, 0x00, 0x80, 0x80, 0x5F, 0x00, 0x40, 0x80, 0x00, 0x00, 0x80, 0x80, 0x00, 0x00}));
            hashtable.put('Í', new UnicodeMap('í',(char)126,new char[]{0x00, 0x00, 0x00, 0x80, 0x10, 0x00, 0x00, 0x80, 0x5F, 0x00, 0x00, 0x80, 0x80, 0x00, 0x00, 0x80, 0x00, 0x00}));
            hashtable.put('Ị', new UnicodeMap('ị',(char)126,new char[]{0x00, 0x00, 0x01, 0x00, 0x20, 0x00, 0x01, 0x00, 0xBE, 0x80, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00}));

            //hashtable.put('Y', new UnicodeMap('y',(char)126,new char[]{0x20, 0x80, 0x10, 0x00, 0x08, 0x00, 0x05, 0x00, 0x02, 0x00, 0x04, 0x00, 0x08, 0x00, 0x10, 0x00, 0x20, 0x00}));
            hashtable.put('Ỳ', new UnicodeMap('ỳ',(char)126,new char[]{0x20, 0x80, 0x10, 0x00, 0x88, 0x00, 0x05, 0x00, 0x42, 0x00, 0x04, 0x00, 0x08, 0x00, 0x10, 0x00, 0x20, 0x00}));
            hashtable.put('Ỷ', new UnicodeMap('ỷ',(char)126,new char[]{0x20, 0x80, 0x10, 0x00, 0x08, 0x00, 0x85, 0x00, 0x02, 0x00, 0xC4, 0x00, 0x08, 0x00, 0x10, 0x00, 0x20, 0x00}));
            hashtable.put('Ỹ', new UnicodeMap('ỹ',(char)126,new char[]{0x20, 0x80, 0x50, 0x00, 0x08, 0x00, 0x85, 0x00, 0x02, 0x00, 0x44, 0x00, 0x08, 0x00, 0x90, 0x00, 0x20, 0x00}));
            hashtable.put('Ý', new UnicodeMap('ý',(char)126,new char[]{0x20, 0x80, 0x10, 0x00, 0x08, 0x00, 0x05, 0x00, 0x42, 0x00, 0x04, 0x00, 0x88, 0x00, 0x10, 0x00, 0x20, 0x00}));
            hashtable.put('Ỵ', new UnicodeMap('ỵ',(char)126,new char[]{0x20, 0x80, 0x10, 0x00, 0x08, 0x00, 0x05, 0x00, 0x02, 0x00, 0x04, 0x80, 0x08, 0x00, 0x10, 0x00, 0x20, 0x00}));


        }
        return  hashtable;
    }

    public static void setHashtable(Hashtable<Character, UnicodeMap> hashtable) {
        UnicodeMap.hashtable = hashtable;
    }
}
