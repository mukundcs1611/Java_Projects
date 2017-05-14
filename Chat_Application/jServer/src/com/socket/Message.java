package com.socket;

import java.io.Serializable;

public class Message implements Serializable{
    
    private static final long serialVersionUID = 1L;
    public String type, sender, content, recipient;
    boolean roomOrUser;
    
    public Message(String type, String sender, String content, String recipient,boolean roomOrUser){
        this.type = type; this.sender = sender; this.content = content; this.recipient = recipient; this.roomOrUser=roomOrUser;
    }
    
    @Override
    public String toString(){
        return "{type='"+type+"', sender='"+sender+"', content='"+content+"', recipient='"+recipient+"' , roomOrUser='"+roomOrUser+"'}";
    }
}
