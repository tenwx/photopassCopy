package com.pictureair.photopass.entity;

import java.io.Serializable;

/**
 * 帮助信息类
 * Created by bass on 15/12/16.
 */
public class HelpInfo implements Serializable {
    public String helpId;
    public String helpQuestionCN;
    public String helpAnswerCN;
    public String helpQuestionEN;
    public String helpAnswerEN;

    public HelpInfo(){}

    public HelpInfo(String helpId, String helpQuestionCN, String helpAnswerCN, String helpQuestionEN, String helpAnswerEN) {
        this.helpId = helpId;
        this.helpQuestionCN = helpQuestionCN;
        this.helpAnswerCN = helpAnswerCN;
        this.helpQuestionEN = helpQuestionEN;
        this.helpAnswerEN = helpAnswerEN;
    }

    public String getHelpId() {
        return helpId;
    }

    public void setHelpId(String helpId) {
        this.helpId = helpId;
    }

    public String getHelpQuestionCN() {
        return helpQuestionCN;
    }

    public void setHelpQuestionCN(String helpQuestionCN) {
        this.helpQuestionCN = helpQuestionCN;
    }

    public String getHelpAnswerCN() {
        return helpAnswerCN;
    }

    public void setHelpAnswerCN(String helpAnswerCN) {
        this.helpAnswerCN = helpAnswerCN;
    }

    public String getHelpQuestionEN() {
        return helpQuestionEN;
    }

    public void setHelpQuestionEN(String helpQuestionEN) {
        this.helpQuestionEN = helpQuestionEN;
    }

    public String getHelpAnswerEN() {
        return helpAnswerEN;
    }

    public void setHelpAnswerEN(String helpAnswerEN) {
        this.helpAnswerEN = helpAnswerEN;
    }
}