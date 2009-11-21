/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.internal.apt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.seasar.doma.expr.ExpressionFunctions;
import org.seasar.doma.internal.apt.dao.ExpressionValidationDao;
import org.seasar.doma.internal.apt.decl.TypeDeclaration;
import org.seasar.doma.internal.apt.entity.Emp;
import org.seasar.doma.internal.apt.util.ElementUtil;
import org.seasar.doma.internal.apt.util.TypeMirrorUtil;
import org.seasar.doma.internal.expr.ExpressionParser;
import org.seasar.doma.internal.expr.node.ExpressionNode;
import org.seasar.doma.internal.message.DomaMessageCode;

public class ExpressionValidatorTest extends AptTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        addSourcePath("src/main/java");
        addSourcePath("src/test/java");
    }

    public void testVariable_notFound() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser("notFound").parse();
        try {
            validator.validate(node);
            fail();
        } catch (AptException expected) {
            System.out.println(expected);
            assertEquals(DomaMessageCode.DOMA4067, expected.getMessageCode());
        }
    }

    public void testMethod_found() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser("emp.equals(emp)").parse();
        TypeDeclaration result = validator.validate(node);
        assertTrue(result.isBooleanType());
    }

    public void testMethod_notFound() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser(
                "emp.notFound(1, \"aaa\".length())").parse();
        try {
            validator.validate(node);
            fail();
        } catch (AptException expected) {
            System.out.println(expected);
            assertEquals(DomaMessageCode.DOMA4071, expected.getMessageCode());
        }
    }

    public void testMethod_foundFromCandidates() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser("emp.hoge(\"aaa\")").parse();
        TypeDeclaration result = validator.validate(node);
        assertTrue(result.isNumberType());
    }

    public void testMethod_notFoundFromCandidates() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser(
                "emp.hoge(new java.lang.Integer(1))").parse();
        try {
            validator.validate(node);
            fail();
        } catch (AptException expected) {
            System.out.println(expected);
            assertEquals(DomaMessageCode.DOMA4073, expected.getMessageCode());
        }
    }

    public void testStaticMethod_found() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        String expression = String.format("@%s@staticMethod(\"aaa\")",
                Emp.class.getName());
        ExpressionNode node = new ExpressionParser(expression).parse();
        TypeDeclaration result = validator.validate(node);
        assertTrue(result.isNumberType());
    }

    public void testStaticMethod_classNotFound() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser("@Xxx@staticMethod(\"aaa\")")
                .parse();
        try {
            validator.validate(node);
            fail();
        } catch (AptException expected) {
            System.out.println(expected);
            assertEquals(DomaMessageCode.DOMA4145, expected.getMessageCode());
        }
    }

    public void testStaticMethod_methodNotFound() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        String expression = String.format("@%s@getName()", Emp.class.getName());
        ExpressionNode node = new ExpressionParser(expression).parse();
        try {
            validator.validate(node);
            fail();
        } catch (AptException expected) {
            System.out.println(expected);
            assertEquals(DomaMessageCode.DOMA4146, expected.getMessageCode());
        }
    }

    public void testStaticField_found() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        String expression = String.format("@%s@staticField", Emp.class
                .getName());
        ExpressionNode node = new ExpressionParser(expression).parse();
        TypeDeclaration result = validator.validate(node);
        assertTrue(result.isTextType());
    }

    public void testStaticField_classNotFound() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser("@Xxx@staticField").parse();
        try {
            validator.validate(node);
            fail();
        } catch (AptException expected) {
            System.out.println(expected);
            assertEquals(DomaMessageCode.DOMA4145, expected.getMessageCode());
        }
    }

    public void testStaticField_fieldNotFound() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        String expression = String.format("@%s@name", Emp.class.getName());
        ExpressionNode node = new ExpressionParser(expression).parse();
        try {
            validator.validate(node);
            fail();
        } catch (AptException expected) {
            System.out.println(expected);
            assertEquals(DomaMessageCode.DOMA4148, expected.getMessageCode());
        }
    }

    public void testFunction_found() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(ExpressionFunctions.class);
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser("@prefix(emp.name)").parse();
        TypeDeclaration result = validator.validate(node);
        assertTrue(result.isTextType());
    }

    public void testFunction_notFound() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(ExpressionFunctions.class);
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser("@hoge(emp.name)").parse();
        try {
            validator.validate(node);
            fail();
        } catch (AptException expected) {
            System.out.println(expected);
            assertEquals(DomaMessageCode.DOMA4072, expected.getMessageCode());
        }
    }

    public void testConstructor_notFound() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser("new java.lang.String(1, 2)")
                .parse();
        try {
            validator.validate(node);
            fail();
        } catch (AptException expected) {
            System.out.println(expected);
            assertEquals(DomaMessageCode.DOMA4115, expected.getMessageCode());
        }
    }

    public void testFieldAccess() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser("emp.id").parse();
        TypeDeclaration result = validator.validate(node);
        assertFalse(result.isUnknownType());
    }

    public void testMethodAccess() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser("emp.getId()").parse();
        TypeDeclaration result = validator.validate(node);
        assertFalse(result.isUnknownType());
    }

    public void testConstructorAccess() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser(
                "emp.id == new java.lang.Integer(1)").parse();
        TypeDeclaration result = validator.validate(node);
        assertFalse(result.isUnknownType());
    }

    public void testMethodAccess_withArguments() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser("emp.add(2, 3)").parse();
        TypeDeclaration result = validator.validate(node);
        assertFalse(result.isUnknownType());
    }

    public void testEqOperator() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser("emp.add(2, 3) == 5")
                .parse();
        TypeDeclaration result = validator.validate(node);
        assertFalse(result.isUnknownType());
    }

    public void testUnreferencedParameter() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser("true").parse();
        validator.validate(node);
        assertFalse(validator.getValidatedParameterNames().contains("emp"));
    }

    public void testAddOperator_number_number() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser("1 + 2").parse();
        TypeDeclaration result = validator.validate(node);
        assertTrue(result.isNumberType());
    }

    public void testAddOperator_number_text() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser("1 + \"2\"").parse();
        try {
            validator.validate(node);
            fail();
        } catch (AptException expected) {
            System.out.println(expected);
            assertEquals(DomaMessageCode.DOMA4121, expected.getMessageCode());
        }
    }

    public void testAddOperator_text_text() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser("\"1\" + \"2\"").parse();
        TypeDeclaration result = validator.validate(node);
        assertTrue(result.isTextType());
    }

    public void testAddOperator_text_number() throws Exception {
        Class<?> target = ExpressionValidationDao.class;
        addCompilationUnit(target);
        compile();

        ExecutableElement methodElement = createMethodElement(target,
                "testEmp", Emp.class);
        Map<String, TypeMirror> parameterTypeMap = createParameterTypeMap(methodElement);
        ExpressionValidator validator = new ExpressionValidator(
                getProcessingEnvironment(), methodElement, parameterTypeMap);

        ExpressionNode node = new ExpressionParser("\"2\" + 1").parse();
        try {
            validator.validate(node);
            fail();
        } catch (AptException expected) {
            System.out.println(expected);
            assertEquals(DomaMessageCode.DOMA4126, expected.getMessageCode());
        }
    }

    protected ExecutableElement createMethodElement(Class<?> clazz,
            String methodName, Class<?>... parameterClasses) {
        ProcessingEnvironment env = getProcessingEnvironment();
        TypeElement typeElement = ElementUtil.getTypeElement(clazz, env);
        for (TypeElement t = typeElement; t != null
                && t.asType().getKind() != TypeKind.NONE; t = TypeMirrorUtil
                .toTypeElement(t.getSuperclass(), env)) {
            for (ExecutableElement methodElement : ElementFilter.methodsIn(t
                    .getEnclosedElements())) {
                if (!methodElement.getSimpleName().contentEquals(methodName)) {
                    continue;
                }
                List<? extends VariableElement> parameterElements = methodElement
                        .getParameters();
                if (parameterElements.size() != parameterClasses.length) {
                    continue;
                }
                for (int i = 0; i < parameterElements.size(); i++) {
                    TypeMirror parameterType = parameterElements.get(i)
                            .asType();
                    Class<?> parameterClass = parameterClasses[i];
                    if (!TypeMirrorUtil.isSameType(parameterType,
                            parameterClass, env)) {
                        return null;
                    }
                }
                return methodElement;
            }
        }
        return null;
    }

    protected Map<String, TypeMirror> createParameterTypeMap(
            ExecutableElement methodElement) {
        Map<String, TypeMirror> result = new HashMap<String, TypeMirror>();
        for (VariableElement parameter : methodElement.getParameters()) {
            String name = parameter.getSimpleName().toString();
            TypeMirror type = parameter.asType();
            result.put(name, type);
        }
        return result;
    }

}
