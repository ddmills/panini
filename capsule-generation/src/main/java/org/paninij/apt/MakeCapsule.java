/*
 * This file is part of the Panini project at Iowa State University.
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * For more details and the latest version of this code please see
 * http://paninij.org
 *
 * Contributor(s): Dalton Mills, David Johnston, Trey Erenberger
 */
package org.paninij.apt;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.paninij.apt.util.PaniniModelInfo;
import org.paninij.apt.util.Source;

/**
 * This class contains logic to inspect a given capsule template class and generate the capsule
 * interface which every concrete capsule type (e.g. `Foo$Thread`, `Bar$Task`) will implement.
 *
 * For example, if the given capsule template is named `Baz$Template`, then this class will make a
 * `Baz` interface. For each procedure defined in `Baz$Template`, an equivalent declarations will
 * be added to the `Baz` interface.
 */
public class MakeCapsule
{
    TypeElement template;
    PaniniProcessor context;

    static MakeCapsule make(PaniniProcessor context, TypeElement template)
    {
        MakeCapsule sig = new MakeCapsule();
        sig.context = context;
        sig.template = template;
        return sig;
    }

    void makeSourceFile()
    {
        context.createJavaFile(buildQualifiedCapsuleName(), buildCapsuleInterface());
    }

    private String buildCapsuleInterface()
    {
        String src = Source.lines(0, "package #0;",
                                     "",
                                     "#1",
                                     "",
                                     "/**",
                                     " * This capsule interface was auto-generated from `#2`",
                                     " */",
                                     "@CapsuleInterface",
                                     "#3",
                                     "{",
                                     "#4",
                                     "}");

        return Source.format(src, buildPackage(),
                                  buildImports(),
                                  PaniniModelInfo.qualifiedTemplateName(template),
                                  buildCapsuleDecl(),
                                  buildCapsuleBody());
    }

    private String buildCapsuleDecl()
    {
        // TODO: Fix format string once GitHub issue #24 is resolved.
        return Source.format("public interface #0 extends #1 ", buildCapsuleName(),
                                                               buildCapsuleInterfaces());
    }

    private String buildCapsuleInterfaces()
    {
        List<String> interfaces = new ArrayList<String>();
        interfaces.add("Panini$Capsule");
        for (TypeMirror i : template.getInterfaces()) {
            interfaces.add(i.toString());
        }
        return String.join(", ", interfaces);
    }

    private String buildCapsuleName()
    {
        return PaniniModelInfo.simpleCapsuleName(template);
    }

    private String buildQualifiedCapsuleName()
    {
        return PaniniModelInfo.qualifiedCapsuleName(template);
    }

    private String buildImports()
    {
        return Source.buildCollectedImportDecls(template,
                                                "org.paninij.lang.CapsuleInterface",
                                                "org.paninij.runtime.Panini$Capsule");
    }

    private String buildPackage()
    {
        return context.getPackageOf(template);
    }

    private String buildCapsuleBody()
    {
        ArrayList<String> decls = new ArrayList<String>();

        if (PaniniModelInfo.hasWiredFieldDecls(context, template)) {
            decls.add("    " + PaniniModelInfo.buildWireMethodDecl(context, template) + ";\n");
        }

        for (Element child : template.getEnclosedElements())
        {
            if (PaniniModelInfo.isProcedure(child))
            {
                ExecutableElement method = (ExecutableElement) child;
                String decl = Source.format("    #0;\n", Source.buildExecutableDecl(method));
                decls.add(decl);
            }
        }
        return String.join("", decls);
    }
}
