public class Main {

    public static void main(String[] args) {
        try {

            // считываем объект из .obj файла
            Object stormTrooper = ObjectUtils.ObjectFromFile("objects/StormTrooper.obj");
            // проективное преобразование
            Picture picture5 = ObjectUtils.ObjectToPicture5(stormTrooper,1000,1000,900, 900,500,500, 5);
            PictureUtils.Save(picture5, "pictures/picture5.png");
            // поворот модели
            ObjectUtils.Rotate(stormTrooper,-0.05*Math.PI,0.93*Math.PI,0);
            Picture picture6 = ObjectUtils.ObjectToPicture5(stormTrooper,1000,1000,1100, 1100,500,500, 6);
            PictureUtils.Save(picture6, "pictures/picture6.png");

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
