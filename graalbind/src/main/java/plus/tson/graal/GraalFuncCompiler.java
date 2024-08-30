package plus.tson.graal;

import plus.tson.TsonFunc;
import plus.tson.utl.FuncCompiler;
import javax.script.ScriptEngine;
import org.graalvm.polyglot.*;


public class GraalFuncCompiler extends FuncCompiler.Compiler {
    Context ctx;
    String name;
    @Override
    public void setEngine(ScriptEngine engine) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScriptEngine getEngine() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void tryInstallEngine(String name) {
        if(ctx == null)try {
            ctx = Context.newBuilder().option("engine.WarnInterpreterOnly", "false").build();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        this.name = name;
    }


    @Override
    public TsonFunc compile(TsonFunc.Frame frame) {
        frame.trim();

        Value value = ctx.parse(name, frame.toString());


        return super.compile(frame);
    }


    @Override
    public TsonFunc.Compiler fork() {
        GraalFuncCompiler compiler = new GraalFuncCompiler();
        if(ctx == null){
            tryInstallEngine(name);
        }
        compiler.ctx  = this.ctx;
        compiler.name = this.name;
        return compiler;
    }
}


class GraalFunc implements TsonFunc{
    private final Value value;
    private final String[] agrs;

    GraalFunc(GraalFuncCompiler compiler, TsonFunc.Frame frame) {
        this.value = compiler.ctx.parse(compiler.name, frame.trim().toString());
        this.agrs = frame.getArgs();
    }


    @Override
    public String[] args() {
        return agrs;
    }


    @Override
    public Object call(Object... args) {
        return value.execute(args);
    }


    @Override
    public Object call() {
        return value.execute();
    }
}