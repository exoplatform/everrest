/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.uri;

import org.everrest.core.impl.uri.UriComponent;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class UriPattern {

    /**
     * Sort the templates according to the string comparison of the template regular expressions.
     * <p>
     * JSR-311 specification: "Sort the set of matching resource classes using the number of characters in the regular
     * expression not resulting from template variables as the primary key and the number of matching groups as a
     * secondary key"
     * </p>
     */
    public static final Comparator<UriPattern> URIPATTERN_COMPARATOR = new UriPatternComparator();

    /** URI pattern comparator. */
    private static final class UriPatternComparator implements Comparator<UriPattern> {

        @Override
        public int compare(UriPattern o1, UriPattern o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return 1;
            }
            if (o2 == null) {
                return -1;
            }

            if (o1.getTemplate().isEmpty() && o2.getTemplate().isEmpty()) {
                return 0;
            }
            if (o1.getTemplate().isEmpty()) {
                return 1;
            }
            if (o2.getTemplate().isEmpty()) {
                return -1;
            }

            if (o1.getNumberOfLiteralCharacters() < o2.getNumberOfLiteralCharacters()) {
                return 1;
            }
            if (o1.getNumberOfLiteralCharacters() > o2.getNumberOfLiteralCharacters()) {
                return -1;
            }

            // pattern with two variables less the pattern with four variables
            if (o1.getParameterNames().size() < o2.getParameterNames().size()) {
                return 1;
            }
            if (o1.getParameterNames().size() > o2.getParameterNames().size()) {
                return -1;
            }

            return o1.getRegex().compareTo(o2.getRegex());
        }
    }

    /** Should be added in URI pattern regular expression. */
    private static final String URI_PATTERN_TAIL = "(/.*)?";

    //

    /** List of names for URI template variables. */
    private final List<String> parameterNames;

    /** URI template. */
    private final String template;

    /** Number of characters in URI template NOT resulting from template variable substitution. */
    private final int numberOfCharacters;

    /** Compiled URI pattern. */
    private final Pattern pattern;

    /** Regular expressions for URI pattern. */
    private final String regex;

    /** Regex capturing group indexes. */
    private final int[] groupIndexes;

    //

    /**
     * Constructs UriPattern.
     *
     * @param template
     *         the source template
     * @see {@link javax.ws.rs.Path}
     */
    public UriPattern(String template) {
        if (template.length() > 0 && template.charAt(0) != '/') {
            template = '/' + template;
        }

        UriTemplateParser parser = new UriTemplateParser(template);
        this.template = parser.getTemplate();
        this.parameterNames = Collections.unmodifiableList(parser.getParameterNames());
        this.numberOfCharacters = parser.getNumberOfLiteralCharacters();

        int[] indexes = parser.getGroupIndexes();
        if (indexes != null) {
            this.groupIndexes = new int[indexes.length + 1];
            System.arraycopy(indexes, 0, this.groupIndexes, 0, indexes.length);
            // Add one more index for URI_PATTERN_TAIL
            this.groupIndexes[groupIndexes.length - 1] = indexes[indexes.length - 1] + 1;
        } else {
            this.groupIndexes = null;
        }

        String regex = parser.getRegex();
        if (regex.endsWith("/")) {
            regex = regex.substring(0, regex.length() - 1);
        }
        this.regex = regex + URI_PATTERN_TAIL;
        this.pattern = Pattern.compile(this.regex);
    }


    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == getClass() && getRegex().equals(((UriPattern)obj).getRegex());
    }


    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + regex.hashCode();
        return hash;
    }

    /**
     * Get the regex pattern.
     *
     * @return the regex pattern
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Get the URI template as a String.
     *
     * @return the URI template
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Get the regular expression.
     *
     * @return the regular expression
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Get the number of literal characters in the template.
     *
     * @return number of literal characters in the template
     */
    public int getNumberOfLiteralCharacters() {
        return numberOfCharacters;
    }

    /** @return list of names */
    public List<String> getParameterNames() {
        return parameterNames;
    }

    /**
     * Check is URI string match to pattern. If it is then fill given list by parameter value. Before coping value list
     * is cleared. List will be 1 greater then number of keys. It can be used for check is resource is matching to
     * requested. If resource is match the last element in list must be '/' or null.
     *
     * @param uri
     *         the URI string
     * @param parameters
     *         target list
     * @return true if URI string is match to pattern, false otherwise
     */
    public boolean match(String uri, List<String> parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("list is null");
        }

        if (uri == null || uri.isEmpty()) {
            return pattern == null;
        }
        if (pattern == null) {
            return false;
        }

        Matcher m = pattern.matcher(uri);
        if (!m.matches()) {
            return false;
        }

        parameters.clear();
        if (groupIndexes == null) {
            for (int i = 1; i <= m.groupCount(); i++) {
                parameters.add(m.group(i));
            }
        } else {
            for (int i = 0; i < groupIndexes.length - 1; i++) {
                parameters.add(m.group(groupIndexes[i]));
            }
        }
        return true;
    }


    public String toString() {
        return regex;
    }

    /**
     * Create URI from URI part. Each URI part can contains templates.
     *
     * @param schema
     *         the schema URI part
     * @param userInfo
     *         the user info URI part
     * @param host
     *         the host name URI part
     * @param port
     *         the port number URI part
     * @param path
     *         the path URI part
     * @param query
     *         the query string URI part
     * @param fragment
     *         the fragment URI part
     * @param values
     *         the values which must be used instead templates parameters
     * @param encode
     *         if true then encode value before add it in URI, otherwise value must be validate to legal characters
     * @param asTemplate
     *         if true ignore absence value for any URI parameters
     * @return the URI string
     */
    public static String createUriWithValues(String schema,
                                             String userInfo,
                                             String host,
                                             int port,
                                             String path,
                                             String query,
                                             String fragment,
                                             Map<String, ?> values,
                                             boolean encode,
                                             boolean asTemplate) {
        StringBuilder sb = new StringBuilder();
        if (schema != null) {
            appendUriPart(sb, schema, UriComponent.SCHEME, values, false, asTemplate);
            sb.append(':');
        }
        if (userInfo != null || host != null || port != -1) {
            sb.append('/');
            sb.append('/');

            if (!(userInfo == null || userInfo.isEmpty())) {
                appendUriPart(sb, userInfo, UriComponent.USER_INFO, values, encode, asTemplate);
                sb.append('@');
            }
            if (host != null) {
                appendUriPart(sb, host, UriComponent.HOST, values, encode, asTemplate);
            }

            if (port != -1) {
                sb.append(':');
                appendUriPart(sb, Integer.toString(port), UriComponent.PORT, values, encode, asTemplate);
            }

        }

        if (!(path == null || path.isEmpty())) {
            if (sb.length() > 0 && path.charAt(0) != '/') {
                sb.append('/');
            }
            appendUriPart(sb, path, UriComponent.PATH, values, encode, asTemplate);
        }

        if (!(query == null || query.isEmpty())) {
            sb.append('?');
            appendUriPart(sb, query, UriComponent.QUERY, values, encode, asTemplate);
        }

        if (!(fragment == null || fragment.isEmpty())) {
            sb.append('#');
            appendUriPart(sb, fragment, UriComponent.FRAGMENT, values, encode, asTemplate);
        }

        return sb.toString();
    }

    /**
     * Create URI from URI part. Each URI part can contains templates.
     *
     * @param schema
     *         the schema URI part
     * @param userInfo
     *         the user info URI part
     * @param host
     *         the host name URI part
     * @param port
     *         the port number URI part
     * @param path
     *         the path URI part
     * @param query
     *         the query string URI part
     * @param fragment
     *         the fragment URI part
     * @param values
     *         the values which must be used instead templates parameters
     * @param encode
     *         if true then encode value before add it in URI, otherwise value must be validate to legal characters
     * @return the URI string
     */
    public static String createUriWithValues(String schema,
                                             String userInfo,
                                             String host,
                                             int port,
                                             String path,
                                             String query,
                                             String fragment,
                                             Map<String, ?> values,
                                             boolean encode) {
        return createUriWithValues(schema, userInfo, host, port, path, query, fragment, values, encode, false);
    }

    /**
     * Create URI from URI part. Each URI part can contains templates.
     *
     * @param schema
     *         the schema URI part
     * @param userInfo
     *         the user info URI part
     * @param host
     *         the host name URI part
     * @param port
     *         the port number URI part
     * @param path
     *         the path URI part
     * @param query
     *         the query string URI part
     * @param fragment
     *         the fragment URI part
     * @param values
     *         the values which must be used instead templates parameters
     * @param encode
     *         if true then encode value before add it in URI, otherwise value must be validate to legal characters
     * @param asTemplate
     *         if true ignore absence value for any URI parameters
     * @return the URI string
     */
    public static String createUriWithValues(String schema,
                                             String userInfo,
                                             String host,
                                             int port,
                                             String path,
                                             String query,
                                             String fragment,
                                             Object[] values,
                                             boolean encode,
                                             boolean asTemplate) {
        Map<String, String> m = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        int p = 0;

        if (schema != null) {
            p = appendUriPart(sb, schema, UriComponent.SCHEME, values, p, m, false, asTemplate);
            sb.append(':');
        }
        if (userInfo != null || host != null || port != -1) {
            sb.append('/');
            sb.append('/');

            if (!(userInfo == null || userInfo.isEmpty())) {
                p = appendUriPart(sb, userInfo, UriComponent.USER_INFO, values, p, m, encode, asTemplate);
                sb.append('@');
            }

            if (host != null) {
                p = appendUriPart(sb, host, UriComponent.HOST, values, p, m, encode, asTemplate);
            }

            if (port != -1) {
                sb.append(':');
                p = appendUriPart(sb, Integer.toString(port), UriComponent.PORT, values, p, m, encode, asTemplate);
            }
        }

        if (!(path == null || path.isEmpty())) {
            if (sb.length() > 0 && path.charAt(0) != '/') {
                sb.append('/');
            }
            p = appendUriPart(sb, path, UriComponent.PATH, values, p, m, encode, asTemplate);
        }

        if (!(query == null || query.isEmpty())) {
            sb.append('?');
            p = appendUriPart(sb, query, UriComponent.QUERY, values, p, m, encode, asTemplate);
        }

        if (!(fragment == null || fragment.isEmpty())) {
            sb.append('#');
            appendUriPart(sb, fragment, UriComponent.FRAGMENT, values, p, m, encode, asTemplate);
        }

        return sb.toString();
    }


    /**
     * Create URI from URI part. Each URI part can contains templates.
     *
     * @param schema
     *         the schema URI part
     * @param userInfo
     *         the user info URI part
     * @param host
     *         the host name URI part
     * @param port
     *         the port number URI part
     * @param path
     *         the path URI part
     * @param query
     *         the query string URI part
     * @param fragment
     *         the fragment URI part
     * @param values
     *         the values which must be used instead templates parameters
     * @param encode
     *         if true then encode value before add it in URI, otherwise value must be validate to legal characters
     * @return the URI string
     */
    public static String createUriWithValues(String schema,
                                             String userInfo,
                                             String host,
                                             int port,
                                             String path,
                                             String query,
                                             String fragment,
                                             Object[] values,
                                             boolean encode) {
        return createUriWithValues(schema, userInfo, host, port, path, query, fragment, values, encode, false);
    }

    /**
     * @param sb
     *         the StringBuilder for appending URI part
     * @param uriPart
     *         URI part
     * @param component
     *         the URI component
     * @param values
     *         values map
     * @param encode
     *         if true then encode value before add it in URI, otherwise value must be validate to legal characters
     * @param asTemplate
     *         if true ignore absence value for any URI parameters
     */
    private static void appendUriPart(StringBuilder sb,
                                      String uriPart,
                                      int component,
                                      Map<String, ?> values,
                                      boolean encode,
                                      boolean asTemplate) {
        final int length = uriPart.length();
        int lastTemplate = 0;
        for (int i = 0; i < length; i++) {
            char c = uriPart.charAt(i);
            if (c == '{') {
                sb.append(uriPart, lastTemplate, i);
                lastTemplate = i;
                while (++i < length && (c = uriPart.charAt(i)) != '}') {
                }
                if (c != '}') {
                    throw new IllegalArgumentException("Invalid URI template " + uriPart + ". Opened '{' is not closed by '}'. ");
                }
                String name = uriPart.substring(lastTemplate + 1, i);
                Object o = values.get(name);
                if (o == null) {
                    if (asTemplate) {
                        sb.append('{').append(name).append('}');
                    } else {
                        throw new IllegalArgumentException("Not found corresponding value for parameter " + name);
                    }
                } else {

                    String value = o.toString();
                    sb.append(encode
                              ? UriComponent.encode(value, component, true)
                              : UriComponent.recognizeEncode(value, component, true)
                             );
                }
                lastTemplate = i + 1;
            }
        }
        if (lastTemplate == 0) {
            // There is no any templates in string. Add it as is.
            sb.append(uriPart);
        } else if (lastTemplate < length) {
            // append the tail of uri part
            sb.append(uriPart, lastTemplate, length);
        }
    }

    /**
     * @param sb
     *         the StringBuilder for appending URI part
     * @param uriPart
     *         URI part
     * @param component
     *         the URI component
     * @param sourceValues
     *         the source array of values
     * @param offset
     *         the offset in array
     * @param values
     *         values map, keep parameter/value pair which have been already found. From java docs:
     *         <p>
     *         All instances of the same template parameter will be replaced by the same value that corresponds to the
     *         position of the first instance of the template parameter. e.g. the template "{a}/{b}/{a}" with values
     *         {"x", "y", "z"} will result in the the URI "x/y/x", <i>not</i> "x/y/z".
     *         </p>
     * @param encode
     *         if true then encode value before add it in URI, otherwise value must be validate to legal characters
     * @param asTemplate
     *         if true ignore absence value for any URI parameters
     * @return offset
     */
    private static int appendUriPart(StringBuilder sb,
                                     String uriPart,
                                     int component,
                                     Object[] sourceValues,
                                     int offset,
                                     Map<String, String> values,
                                     boolean encode,
                                     boolean asTemplate) {
        final int length = uriPart.length();
        int lastTemplate = 0;
        for (int i = 0; i < length; i++) {
            char c = uriPart.charAt(i);
            if (c == '{') {
                sb.append(uriPart, lastTemplate, i);
                lastTemplate = i;
                while (++i < length && (c = uriPart.charAt(i)) != '}') {
                }
                if (c != '}') {
                    throw new IllegalArgumentException("Invalid URI template " + uriPart + ". Opened '{' is not closed by '}'. ");
                }
                String paramName = uriPart.substring(lastTemplate + 1, i);

                String value = values.get(paramName);
                if (value != null) {
                    // Value already known, then don't take new one from array.
                    // Value from map is already validate or encoded, so do nothing about it.
                    sb.append(value);
                } else {
                    // Value is unknown, we met it first time, then process it and keep in map.
                    // Value will be encoded (or validate)before putting in map.
                    if (offset < sourceValues.length) {
                        value = sourceValues[offset++].toString();
                    }

                    if (value != null) {
                        value = encode
                                ? UriComponent.encode(value, component, true)
                                : UriComponent.recognizeEncode(value, component, true);
                        values.put(paramName, value);
                        sb.append(value);
                    } else {
                        if (asTemplate) {
                            sb.append('{').append(paramName).append('}');
                        } else {
                            throw new IllegalArgumentException("Not found corresponding value for parameter " + paramName);
                        }
                    }
                }
                lastTemplate = i + 1;
            }
        }
        if (lastTemplate == 0) {
            // There is no any templates in string. Add it as is.
            sb.append(uriPart);
        } else if (lastTemplate < length) {
            // append the tail of uri part
            sb.append(uriPart, lastTemplate, length);
        }
        return offset;
    }
}
