package plus.tson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;


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
            if(data.equals("") || data.charAt(0) != '@'){
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


    public TsonFile(String fileName){
        file = new File(fileName);
        load();
    }


    public TsonFile(File file){
        this.file = file;
    }


    public Annotation getAnnotation(){
        return annotation;
    }


    public TsonFile load(){
        String data = read(file, "");
        Object[] data0 = Annotation.scan(data);
        annotation = (Annotation) data0[0];
        data = (String) data0[1];
        if(annotation.makeBackUp){
            if(data.equals(""))return this;
            write(new File(file.getName()+"_backup"),annotation+data);
        }
        return (TsonFile) init(data);
    }


    public TsonFile save(){
        if(annotation.isReadOnly)return this;
        write(file, annotation+this.toString());
        return this;
    }


    protected static String read(File file, String def){
        if(!file.exists()){
            try {
                file.createNewFile();
                if(def!=null){
                    write(file, def);
                }
                System.out.printf("File [%s] not founded!%n", file.toPath());
            } catch (IOException ignored) {}
            return def;
        } else {
            try {
                String data = new String(Files.readAllBytes(file.toPath())).trim();
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