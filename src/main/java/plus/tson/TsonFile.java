package plus.tson;

import plus.tson.security.ClassManager;
import plus.tson.utl.Tuple;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;


public class TsonFile extends TsonMap {
    private final File file;
    private Annotation annotation = new Annotation();

    enum AnnotationKey {
        ReadOnly("@ReadOnly"),
        MakeBackUp("@Backup");
        private final String KEY;

        AnnotationKey(String key) {
            KEY = key;
        }
    }

    public static class Annotation {
        public final boolean isReadOnly;
        public final boolean makeBackUp;


        private Annotation(){
            this(false, false);
        }


        private Annotation(boolean isReadOnly, boolean makeBackUp) {
            this.isReadOnly = isReadOnly;
            this.makeBackUp = makeBackUp;
        }


        static Object[] scan(String data){
            return scan(data, null, null);
        }


        static Object[] scan(String data0, Boolean ro, Boolean mb){
            String data = data0;
            data = data.trim();
            if(data.isEmpty() || data.charAt(0) != '@'){
                return new Object[]{
                        new Annotation(ro != null && ro, mb != null && mb),data};
            }
            if(ro==null && data.startsWith(AnnotationKey.ReadOnly.KEY)){
                data = data.substring(AnnotationKey.ReadOnly.KEY.length()).trim();
                ro = true;
            }
            if(mb==null && data.startsWith(AnnotationKey.MakeBackUp.KEY)){
                data = data.substring(AnnotationKey.MakeBackUp.KEY.length()).trim();
                mb = true;
            }
            if(data.equals(data0)){
                for(int i=0;i<data.length();i++){
                    char c = data.charAt(i);
                    if(c==' ' || c == '\n' || c =='{'){
                        data = data.substring(i);
                        break;
                    }
                }
            }
            return scan(data, ro, mb);
        }

        @Override
        public String toString(){
            return ""+(isReadOnly?AnnotationKey.ReadOnly.KEY+"\n":"")
                    +(makeBackUp?AnnotationKey.MakeBackUp.KEY+"\n":"");
        }
    }


    public File getFile(){
        return file;
    }


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


    @Override
    public TsonFile clone() {
        return (TsonFile) super.clone();
    }


    public Annotation getAnnotation(){
        return annotation;
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
        String data = read(file, def);
        Object[] data0 = Annotation.scan(data);
        annotation = (Annotation) data0[0];
        data = (String) data0[1];
        if(annotation.makeBackUp){
            if(data.equals(""))return this;
            write(new File(file.getName()+"_backup"),annotation+data);
        }
        new TsonParser(manager,
                data.replace("\r","").replace("\t","    ")
        ).goTo('{').fillMap(this);
        return this;
    }


    public static void fillMap(TsonMap map, ClassManager manager, File file, String def){
        String data = read(file, def);
        Object[] data0 = Annotation.scan(data);
        Annotation annotation = (Annotation) data0[0];
        if(map instanceof TsonFile){
            ((TsonFile) map).annotation = annotation;
        }
        data = (String) data0[1];
        if(annotation.makeBackUp){
            if(data.equals(""))return;
            write(new File(file.getName()+"_backup"),annotation+data);
        }
        new TsonParser(manager,
                data.replace("\r","").replace("\t","    ")
        ).goTo('{').fillMap(map);
    }


    public TsonFile save(){
        if(annotation.isReadOnly)return this;
        write(file, annotation+this.toString());
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


    public static void write(File file, String data){
        try(FileWriter writer = new FileWriter(file, false)) {
            writer.write(data);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}