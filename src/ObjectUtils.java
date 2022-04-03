import java.io.FileReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

public class ObjectUtils {

    // считывание объекта из .obj файла
    public static Object ObjectFromFile(String filename) throws Exception {

        Object object=new Object();
        String string;
        Scanner lineScanner;

        FileReader fileReader= new FileReader(filename);
        Scanner fileScanner = new Scanner(fileReader);

        while (fileScanner.hasNextLine()) {

            string=fileScanner.nextLine();

            // пропускаем пустые строки
            if(string.length()<3) {
                continue;
            }

            // если это строка вершины
            if(string.charAt(0)=='v'&&string.charAt(1)==' '){
                lineScanner = new Scanner(string.substring(1)).useLocale(Locale.ENGLISH);
                // считываем координаты
                object.addCoordinate(new Coordinate(lineScanner.nextDouble(),lineScanner.nextDouble(),lineScanner.nextDouble()));
            }

            // если это строка полигона
            else if(string.charAt(0)=='f'){
                Polygon currPolygon=new Polygon();
                int v=0, vt=0, vn=0, k=0, currInt=0;
                boolean isNeg=false;
                // проходим по всей строке
                for(int i=2; i<string.length(); i++){
                    char currChar=string.charAt(i);
                    // если закончили считывать текущее значение
                    if(currChar==' '||currChar=='/'){
                        // если мы считали число
                        if(currInt!=0){
                            if(isNeg) currInt*=-1;
                            // понимаем, за какой компонент вершины отвечает данное число
                            if(k==0) v=currInt;
                            else if(k==1) vt=currInt;
                            else vn=currInt;
                            currInt=0;
                        }
                        isNeg=false;
                        // если закончили считывать вершины - сохраняем её
                        if(currChar==' '){
                            k=0;
                            if(v!=0)
                                currPolygon.addPeak(new Peak(v, vt, vn));
                            v=0; vt=0; vn=0;
                        }
                        else k++;
                    }
                    // если текущий символ '-' - запоминаем это
                    else if(currChar=='-') isNeg = true;
                        // если считываем цифру, записываем ее в текущее число
                    else if(currChar>='0'&&currChar<='9')
                        currInt=currInt*10+currChar-'0';
                }
                // после считывания строки сохраняем последнюю вершину при необходимости
                if(currInt!=0){
                    if(isNeg) currInt*=-1;
                    // понимаем, за какой компонент вершины отвечает данное число
                    if(k==0) v=currInt;
                    else if(k==1) vt=currInt;
                    else vn=currInt;
                    currInt=0;
                }
                if(v!=0)
                    currPolygon.addPeak(new Peak(v, vt, vn));
                // добавляем полигон в список полигонов
                object.addPolygon(currPolygon);
            }
        }
        fileReader.close();
        return object;
    }

    // проективное преобразование
    public static Picture ObjectToPicture5(Object object, int w, int h, double ax, double ay, double x0, double y0, double zShift){
        Picture picture=new Picture(w,h);
        ArrayList<Coordinate> coordinates=object.getCoordinates();
        ArrayList<Polygon> polygons= object.getPolygons();
        // проходим по всем полигонам
        for(int i=0; i<polygons.size();i++){
            ArrayList<Peak> peaks = polygons.get(i).getPeaks();
            // рисуем все треугольники с двумя соседними вершинами и нулевой вершиной
            int v0=peaks.get(0).getP()>0?peaks.get(0).getP()-1:coordinates.size()+peaks.get(0).getP();
            for(int j=1; j<peaks.size()-1; j++){
                int v2=peaks.get(j).getP()>0?peaks.get(j).getP()-1:coordinates.size()+peaks.get(j).getP();
                int v1=peaks.get(j+1).getP()>0?peaks.get(j+1).getP()-1:coordinates.size()+peaks.get(j+1).getP();
                double normal=MathTools.normDotProduct(MathTools.normal(coordinates.get(v0),coordinates.get(v1),coordinates.get(v2)),new Coordinate(0,0,1));
                if(normal<0){
                    double z0=coordinates.get(v0).getZ()+zShift;
                    double z1=coordinates.get(v1).getZ()+zShift;
                    double z2=coordinates.get(v2).getZ()+zShift;
                    PictureUtils.TriangleZ(picture,
                            new Coordinate(coordinates.get(v0).getX()*ax/z0+x0, coordinates.get(v1).getX()*ax/z1+x0, coordinates.get(v2).getX()*ax/z2+x0),
                            new Coordinate(coordinates.get(v0).getY()*ay/z0+y0, coordinates.get(v1).getY()*ay/z1+y0, coordinates.get(v2).getY()*ay/z2+y0),
                            new Coordinate(z0, z1, z2),
                            new Color((int)Math.round(-200*normal), (int)Math.round(-200*normal), (int)Math.round(-200*normal)));
                }
            }
        }
        return picture;
    }

    // поворот модели
    public static void Rotate(Object object, double alpha, double beta, double gamma){
        double sa=Math.sin(alpha);
        double ca=Math.cos(alpha);
        double sb=Math.sin(beta);
        double cb=Math.cos(beta);
        double sg=Math.sin(gamma);
        double cg=Math.cos(gamma);
        double x=0, y=0, z=0;
        ArrayList<Coordinate> origCoordinates=object.getCoordinates();
        ArrayList<Coordinate> newCoordinates=new ArrayList<Coordinate>();
        for (int i=0; i< origCoordinates.size(); i++){
            x=origCoordinates.get(i).getX();
            y=origCoordinates.get(i).getY();
            z=origCoordinates.get(i).getZ();
            newCoordinates.add(new Coordinate(
                    x*cb*cg+y*cb*sg+z*sb,
                    x*(-sa*sb*cg-ca*sg)+y*(-sa*sb*sg+ca*cg)+z*sa*cb,
                    x*(-ca*sb*cg+sa*sg)+y*(-ca*sb*sg-sa*cg)+z*ca*cb
            ));}
        object.setCoordinates(newCoordinates);
    }
}
