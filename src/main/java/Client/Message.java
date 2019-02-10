package Client;

import java.io.Serializable;

public class Message implements Serializable {
    private String messText;

    public Message(String messText) {
        this.messText = messText;
    }

    public String getMessText() {
        return messText;
    }

    public void setMessText(String messText) {
        this.messText = messText;
    }

    @Override
    public String toString() {
        return messText ;
    }
}