package kesh.yoword;

public class ScreenItem {

    String title, description;
    int screenImage;

    public ScreenItem(String title, String description, int screenImage) {
        this.title = title;
        this.description = description;
        this.screenImage = screenImage;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setScreenImage(int screenImage) {
        this.screenImage = screenImage;
    }

    public String getTitle() {
        return title;
    }

    public int getScreenImage() {
        return screenImage;
    }

    public String getDescription() {
        return description;
    }
}
