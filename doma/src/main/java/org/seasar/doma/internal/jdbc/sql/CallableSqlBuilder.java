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
package org.seasar.doma.internal.jdbc.sql;

import static org.seasar.doma.internal.util.AssertionUtil.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.SqlLogFormattingFunction;
import org.seasar.doma.wrapper.Wrapper;

/**
 * @author taedium
 * 
 */
public class CallableSqlBuilder
        implements
        CallableSqlParameterVisitor<Void, CallableSqlBuilder.Context, RuntimeException> {

    protected final Config config;

    protected final ResultParameter<?> resultParameter;

    protected final List<CallableSqlParameter> parameters;

    protected final String moduleName;

    protected final SqlLogFormattingFunction formattingFunction;

    protected boolean began;

    public CallableSqlBuilder(Config config, String moduleName,
            List<CallableSqlParameter> parameters) {
        this(config, moduleName, parameters, null);
    }

    public CallableSqlBuilder(Config config, String moduleName,
            List<CallableSqlParameter> parameters,
            ResultParameter<?> resultParameter) {
        assertNotNull(config, parameters, moduleName);
        this.config = config;
        this.resultParameter = resultParameter;
        this.parameters = parameters;
        this.moduleName = moduleName;
        this.formattingFunction = new ConvertToLogFormatFunction();
    }

    public CallableSql build() {
        Context context = new Context();
        context.append("{");
        if (resultParameter != null) {
            resultParameter.accept(this, context);
            context.append("= ");
        }
        context.append("call ");
        context.append(moduleName);
        context.append("(");
        for (CallableSqlParameter parameter : parameters) {
            parameter.accept(this, context);
        }
        context.cutBackIfNecessary();
        context.append(")}");
        LinkedList<CallableSqlParameter> allParameters = new LinkedList<CallableSqlParameter>(
                parameters);
        if (resultParameter != null) {
            allParameters.addFirst(resultParameter);
        }
        return new CallableSql(context.getSqlBuf(), context
                .getFormattedSqlBuf(), allParameters);
    }

    @Override
    public Void visitBasicResultParameter(BasicResultParameter<?> parameter,
            Context p) throws RuntimeException {
        handelResultParameter(parameter, p);
        return null;
    }

    @Override
    public Void visitDomainResultParameter(
            DomainResultParameter<?, ?> parameter, Context p)
            throws RuntimeException {
        handelResultParameter(parameter, p);
        return null;
    }

    @Override
    public Void visitBasicListResultParameter(
            BasicListResultParameter<?> parameter, Context p)
            throws RuntimeException {
        handelResultParameter(parameter, p);
        return null;
    }

    @Override
    public Void visitDomainListResultParameter(
            DomainListResultParameter<?, ?> parameter, Context p)
            throws RuntimeException {
        handelResultParameter(parameter, p);
        return null;
    }

    @Override
    public Void visitEntityListResultParameter(
            EntityListResultParameter<?> parameter, Context p)
            throws RuntimeException {
        handelResultParameter(parameter, p);
        return null;
    }

    protected void handelResultParameter(ResultParameter<?> parameter, Context p) {
        p.appendRawSql("? ");
        p.appendFormattedSql("? ");
    }

    @Override
    public Void visitBasicListParameter(BasicListParameter<?> parameter,
            Context p) throws RuntimeException {
        handelListParameter(parameter, p);
        return null;
    }

    @Override
    public Void visitDomainListParameter(DomainListParameter<?, ?> parameter,
            Context p) throws RuntimeException {
        handelListParameter(parameter, p);
        return null;
    }

    @Override
    public Void visitEntityListParameter(EntityListParameter<?> parameter,
            Context p) throws RuntimeException {
        handelListParameter(parameter, p);
        return null;
    }

    protected void handelListParameter(ListParameter<?> parameter, Context p) {
        if (config.getDialect().supportsResultSetReturningAsOutParameter()) {
            p.appendRawSql("?, ");
            p.appendFormattedSql("?, ");
            p.addParameter(parameter);
        }
    }

    @Override
    public Void visitBasicInParameter(BasicInParameter parameter, Context p)
            throws RuntimeException {
        handleInParameter(parameter, parameter.getWrapper(), p);
        return null;
    }

    @Override
    public Void visitDomainInParameter(DomainInParameter<?, ?> parameter,
            Context p) throws RuntimeException {
        handleInParameter(parameter, parameter.getWrapper(), p);
        return null;
    }

    @Override
    public Void visitBasicInOutParameter(BasicInOutParameter<?> parameter,
            Context p) throws RuntimeException {
        handleInParameter(parameter, parameter.getWrapper(), p);
        return null;
    }

    @Override
    public Void visitDomainInOutParameter(DomainInOutParameter<?, ?> parameter,
            Context p) throws RuntimeException {
        handleInParameter(parameter, parameter.getWrapper(), p);
        return null;
    }

    protected void handleInParameter(InParameter parameter, Wrapper<?> wrapper,
            Context p) {
        p.appendRawSql("?, ");
        p.appendFormattedSql(wrapper.accept(config.getDialect()
                .getSqlLogFormattingVisitor(), formattingFunction));
        p.appendFormattedSql(", ");
        p.addParameter(parameter);
    }

    @Override
    public Void visitBasicOutParameter(BasicOutParameter<?> parameter, Context p)
            throws RuntimeException {
        handleOutParameter(parameter, p);
        return null;
    }

    @Override
    public Void visitDomainOutParameter(DomainOutParameter<?, ?> parameter,
            Context p) throws RuntimeException {
        handleOutParameter(parameter, p);
        return null;
    }

    protected void handleOutParameter(OutParameter parameter, Context p) {
        p.appendRawSql("?, ");
        p.appendFormattedSql("?, ");
        p.addParameter(parameter);
    }

    protected class Context {

        private final StringBuilder rawSqlBuf = new StringBuilder(200);

        private final StringBuilder formattedSqlBuf = new StringBuilder(200);

        private final List<CallableSqlParameter> contextParameters = new ArrayList<CallableSqlParameter>();

        protected void append(CharSequence sql) {
            appendRawSql(sql);
            appendFormattedSql(sql);
        }

        protected void cutBackIfNecessary() {
            if (!contextParameters.isEmpty()) {
                rawSqlBuf.setLength(rawSqlBuf.length() - 2);
                formattedSqlBuf.setLength(formattedSqlBuf.length() - 2);
            }
        }

        protected void appendRawSql(CharSequence sql) {
            rawSqlBuf.append(sql);
        }

        protected void appendFormattedSql(CharSequence sql) {
            formattedSqlBuf.append(sql);
        }

        protected CharSequence getSqlBuf() {
            return rawSqlBuf;
        }

        protected CharSequence getFormattedSqlBuf() {
            return formattedSqlBuf;
        }

        protected void addParameter(CallableSqlParameter parameter) {
            contextParameters.add(parameter);
        }

        @Override
        public String toString() {
            return rawSqlBuf.toString();
        }
    }
}
