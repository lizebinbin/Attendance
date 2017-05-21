package com.moore.attendance.bean;

import java.io.Serializable;

/**
 * Created by MooreLi on 2017/3/1.
 */

public class CardInfo implements Serializable{
    private String cardId;
    private String name;

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
