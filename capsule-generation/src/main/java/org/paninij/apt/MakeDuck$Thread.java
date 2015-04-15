package org.paninij.apt;

import org.paninij.apt.util.DuckShape;
import org.paninij.apt.util.Source;

public class MakeDuck$Thread extends MakeDuck
{
    public static MakeDuck$Thread make(PaniniPress context) 
    {
        MakeDuck$Thread m = new MakeDuck$Thread();
        m.context = context;
        return m;
    }
    
    @Override
    public void makeSourceFile(DuckShape currentDuck)
    {
        try {
            context.createJavaFile(buildQualifiedClassName(currentDuck),
                                   buildDuck(currentDuck));
        } catch (UnsupportedOperationException ex) {
            context.warning(ex.toString());
        }
    }

    @Override
    String buildNormalDuck(DuckShape currentDuck)
    {
        String src = Source.lines(0, 
                "package #0;",
                "",
                "import org.paninij.runtime.Panini$Message;",
                "import org.paninij.runtime.Panini$Future;",
                "import #1;",
                "",
                "public class #2 extends #4 implements Panini$Message, Panini$Future<#4> {",
                "    public final int panini$procID;",
                "    private #4 panini$result = null;",
                "    boolean panini$isResolved = false;",
                "",
                "#5",
                "",
                "#3",
                "",
                "    @Override",
                "    public int panini$msgID() {",
                "        return panini$procID;",
                "    }",
                "",
                "    @Override",
                "    public void panini$resolve(#4 result) {",
                "        synchronized (this) {",
                "            panini$result = result;",
                "            panini$isResolved = true;",
                "            this.notifyAll();",
                "        }",
                "#6",
                "    }",
                "",
                "    @Override",
                "    public #4 panini$get() {",
                "        while (panini$isResolved == false) {",
                "            try {",
                "                synchronized (this) {",
                "                    while (panini$isResolved == false) this.wait();",
                "                }",
                "            } catch (InterruptedException e) { /* try waiting again */ }",
                "         }",
                "         return panini$result;",
                "    }",
                "",
                "    /* The following override the methods of `#4` */",
                "#7",
                "}");
        return Source.format(src, this.buildPackage(currentDuck),
                                  currentDuck.getQualifiedReturnType(),
                                  this.buildClassName(currentDuck),
                                  this.buildConstructor(currentDuck),
                                  currentDuck.getSimpleReturnType(),
                                  this.buildParameterFields(currentDuck),
                                  this.buildReleaseArgs(currentDuck),
                                  this.buildFacades(currentDuck));
    }

    @Override
    String buildVoidDuck(DuckShape currentDuck)
    {
        String src = Source.lines(0, "package #0;",
                                     "",
                                     "import org.paninij.runtime.Panini$Message;",
                                     "",
                                     "public class #1 implements Panini$Message {",
                                     "    public final int panini$procID;",
                                     "#2",
                                     "",
                                     "#3",
                                     "",
                                     "    @Override",
                                     "    public int panini$msgID() {",
                                     "        return panini$procID;",
                                     "    }",
                                     "}");
        
        return Source.format(src, buildPackage(currentDuck),
                                  buildClassName(currentDuck), 
                                  buildParameterFields(currentDuck),
                                  buildConstructor(currentDuck));
    }

    @Override
    String buildPaniniCustomDuck(DuckShape currentDuck)
    {
        // TODO: Make this handle more than just `String`.
        assert(currentDuck.returnType.toString().equals("org.paninij.lang.String"));

        String src = Source.lines(0, "package #0;",
                                     "",
                                     "import org.paninij.lang.String;",
                                     "",
                                     "public class #1 extends String",
                                     "{",
                                     "    private int panini$procID;",
                                     "",
                                     "#2",
                                     "",
                                     "}");
        return Source.format(src, buildPackage(currentDuck),
                                  buildClassName(currentDuck),
                                  buildConstructor(currentDuck, "        super(\"\");\n"));
    }
   

    @Override
    String buildClassName(DuckShape currentDuck)
    {
        return currentDuck.toString() + "$Thread";
    }

    @Override
    String buildQualifiedClassName(DuckShape currentDuck)
    {
        return buildPackage(currentDuck) + "." + currentDuck.toString() + "$Thread";
    }
    
    
    @Override
    String buildConstructor(DuckShape currentDuck)
    {
        return buildConstructor(currentDuck, "");
    }

    String buildConstructor(DuckShape currentDuck, String prependToBody)
    {
       String constructor = buildConstructorDecl(currentDuck);
       constructor += prependToBody;
       constructor += "        panini$procID = procID;\n";
       for(int i = 0; i < currentDuck.slotTypes.size(); i++)
       {
           constructor += "        panini$arg" + i + " = arg" + i +";\n";
       }
       constructor += "    }";
       return constructor;
    }

    @Override
    String buildConstructorDecl(DuckShape currentDuck)
    {
        String constructorDecl = "    public " + buildClassName(currentDuck) + "(int procID";
        for(int i = 0; i < currentDuck.slotTypes.size(); i++)
        {
            constructorDecl += ", " + currentDuck.slotTypes.get(i) + " arg" + i;
        }
        constructorDecl += ") {\n";
        
        return constructorDecl;
    }

}
