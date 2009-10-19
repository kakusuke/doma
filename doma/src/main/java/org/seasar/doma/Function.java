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
package org.seasar.doma;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Statement;
import java.util.List;

import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.JdbcException;

/**
 * ストアドファンクションの呼び出しを示します。
 * <p>
 * このアノテーションが注釈されるメソッドは、{@link Dao} が注釈されたインタフェースのメンバでなければいけません。
 * 
 * 注釈されるメソッドは、次の制約を満たす必要があります。
 * <ul>
 * <li>パラメータは0個以上である。
 * <li>パラメータには、パラメータの種別を示す {@link In} 、 {@link InOut} 、 {@link Out} 、
 * {@link ResultSet} のいずれかのアノテーションが必須である。これらは、ストアドファンクションの定義に合わせて注釈しなければいけない。
 * <ul>
 * <li>{@code In}は、INパラメータを表す。
 * <li> {@code InOut}は、INOUTパラメータを表す。
 * <li> {@code Out}は、 OUTパラメータを表す。
 * <li> {@code ResultSet} は、カーソルのOUTパラメータ、もしくはストアドファンクションが返す結果セットを表す。
 * </ul>
 * <li>戻り値の型には次のいずれかを指定できる。
 * <ul>
 * <li>{@code void}
 * <li>基本型。
 * <li>{@link Domain} が注釈されたクラス。
 * <li>{@link Entity} が注釈されたクラス。
 * <li>{@link List}。実型引数は、基本型、{@code Domain} が注釈されたクラス、もしくは{@code Entity}
 * が注釈されたクラスなければならない。ただし、戻り値を{@code List}
 * にできるのは、ストアドファンクションがカーソルをファンクションの実行結果として返す場合のみである。
 * </ul>
 * </ul>
 * 
 * <h5>例:</h5>
 * 
 * <pre>
 * &#064;Dao(config = AppConfig.class)
 * public interface EmployeeDao {
 * 
 *     &#064;Function
 *     BigDecimal getSalary(@In Integer id, @Out Reference&lt;String&gt; name);
 * }
 * </pre>
 * 
 * 注釈されるメソッドは、次の例外をスローすることがあります。
 * <ul>
 * <li> {@link DomaNullPointerException} パラメータに {@code null}を渡した場合
 * <li> {@link JdbcException} JDBCに関する例外が発生した場合
 * </ul>
 * 
 * @author taedium
 * @see In
 * @see InOut
 * @see Out
 * @see ResultSet
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@DaoMethod
public @interface Function {

    /**
     * カタログ名です。
     */
    String catalog() default "";

    /**
     * スキーマ名です。
     */
    String schema() default "";

    /**
     * ストアドファンクションの名前です。
     * <p>
     * 指定しない場合、注釈されたメソッドの名前が使用されます。
     */
    String name() default "";

    /**
     * クエリタイムアウト（秒）です。
     * <p>
     * 指定しない場合、{@link Config#getQueryTimeout()}が使用されます。
     * 
     * @see Statement#setQueryTimeout(int)
     */
    int queryTimeout() default -1;
}
