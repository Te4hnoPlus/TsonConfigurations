package plus.tson;

import plus.tson.security.ClassManager;
import plus.tson.utl.Tuple;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


/**
 * Associated with the file Dictionary type String -> TsonObject
 */
public class TsonFile extends TsonMap {
    private final File file;

    public TsonFile(String fileName){
        file = new File(fileName);
        load();
    }


    public TsonFile(ClassManager manager, String fileName){
        file = new File(fileName);
        load(manager);
    }


    public TsonFile(File file){
        this.file = file;
    }


    public File getFile(){
        return file;
    }


    @Override
    public TsonFile clone() {
        return (TsonFile) super.clone();
    }


    public TsonFile load(){
        return load(new ClassManager.Def());
    }


    public TsonFile load(ClassManager manager){
        return load(manager, "");
    }


    public TsonFile load(String def){
        return this.load(new ClassManager.Def(), def);
    }


    public TsonFile load(ClassManager manager, String def){
        parse(manager, read(file, def));
        return this;
    }


    protected void parse(ClassManager manager, String data){
        new TsonParser(manager, remBadChars(data)).goTo('{').fillMap(this);
    }


    public static String remBadChars(String src){
        return src.replace("\r","").replace("\t"," ");
    }


    public static void fillMap(TsonMap map, ClassManager manager, File file, String def){
        new TsonParser(manager,
                read(file, def).replace("\r","").replace("\t","    ")
        ).goTo('{').fillMap(map);
    }


    public TsonFile save(){
        write(file, this.toString());
        return this;
    }


    public static Tuple<File, String> read(File dir, String path, String def){
        File file = new File(dir, path+".tson");
        return new Tuple<>(file, read(file, def));
    }


    public static String read(String file){
        return read(new File(file), "");
    }


    public static boolean createPathIfNeed(File file){
        File parent = file.getParentFile();
        if(parent != null){
            return parent.mkdirs();
        }
        return false;
    }


    public static byte[] readFile(File file){
        try {
            return Files.readAllBytes(file.toPath());
//            FileInputStream is = new FileInputStream(file);
//            byte[] result = is.readAllBytes();
//            is.close();
//            return result;
        } catch (Exception e){
            e.printStackTrace();
            return new byte[0];
        }
    }


    public static String read(File file, String def){
        if(!file.exists()){
            try {
                createPathIfNeed(file);
                file.createNewFile();
                if(def!=null){
                    write(file, def);
                }
                System.out.printf("File [%s] not founded!%n", file.toPath());
            } catch (IOException ignored) {}
            return def;
        } else {
            try {
                String data = new String(readFile(file), StandardCharsets.UTF_8);
                if(data.equals("")){
                    if(def!=null && !"".equals(def)){
                        System.out.println(".tson is empty! write default.");
                        write(file, def);
                        return def;
                    }
                }
                return data;
            }catch (Exception e){
                System.out.println("Error in parse blocks! using default.");
                e.printStackTrace();
                return def;
            }
        }
    }


    public static void write(File file, byte[] data){
        try {
            if(data==null)data = new byte[0];
            Files.write(file.toPath(), data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void write(File file, String data){
        if(data==null)data = "";
        try(FileWriter writer = new FileWriter(file, false)) {
            writer.write(data);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}