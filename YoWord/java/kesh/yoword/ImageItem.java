package kesh.yoword;

public class ImageItem {

    private int imageSrc;
    private String imageName;

    public ImageItem(int image, String name) {
        this.imageSrc = image;
        this.imageName = name;
    }

    public int getImageSrc() {
        return imageSrc;
    }

    public String getImageName() {
        return imageName;
    }
}
