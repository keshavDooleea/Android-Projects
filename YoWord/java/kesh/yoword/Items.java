package kesh.yoword;

import android.widget.TextView;

public class Items {

    private int image;
    private String titlee, note, imageName, date, name, photo;
    private boolean photoType;

    public Items(int imageSrc, String imageName, String title, String note, String date, String name, String photo, boolean photoType) {
        this.image = imageSrc;
        this.imageName = imageName;
        this.titlee = title;
        this.note = note;
        this.date = date;
        this.name = name;
        this.photo = photo;
        this.photoType = photoType;
    }

    public boolean getPhotoType() {
        return photoType;
    }

    public void setPhotoType(boolean photoType) {
        this.photoType = photoType;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getDate() {
        return date;
    }

    public int getImage() {
        return image;
    }

    public String getImageName() {
        return imageName;
    }

    public String getTitlee() {
        return titlee;
    }

    public String getNote() {
        return note;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setTitlee(String titlee) {
        this.titlee = titlee;
    }

    @Override
    public String toString() {
        return "Title  : " + titlee + "\n" +
                "Note   : " + note + "\n"  + "\n" ;
    }
}
