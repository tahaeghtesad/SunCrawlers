/**
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.neo4j.jdbc.rest;

import java.math.BigDecimal;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author mh
 * @since 10.02.14
 */
public class JsonUtils
{

    static ObjectNode serialize( Map<String, Object> parameters, ObjectMapper mapper )
    {
        ObjectNode params = mapper.createObjectNode();
        for ( Map.Entry<String, Object> entry : parameters.entrySet() )
        {
            final String name = entry.getKey();
            final Object value = entry.getValue();
            if ( value == null )
            {
                params.putNull( name );
            }
            else if ( value instanceof String )
            {
                params.put( name, value.toString() );
            }
            else if ( value instanceof Integer )
            {
                params.put( name, (Integer) value );
            }
            else if ( value instanceof Long )
            {
                params.put( name, (Long) value );
            }
            else if ( value instanceof Boolean )
            {
                params.put( name, (Boolean) value );
            }
            else if ( value instanceof BigDecimal )
            {
                params.put( name, (BigDecimal) value );
            }
            else if ( value instanceof Double )
            {
                params.put( name, (Double) value );
            }
            else if ( value instanceof byte[] )
            {
                params.put( name, (byte[]) value );
            }
            else if ( value instanceof Float )
            {
                params.put( name, (Float) value );
            }
            else if ( value instanceof Number )
            {
                final Number number = (Number) value;
                if ( number.longValue() == number.doubleValue() )
                {
                    params.put( name, number.longValue() );
                }
                else
                {
                    params.put( name, number.doubleValue() );
                }
            }
            else if ( value instanceof Map )
            {
                params.put( name, serialize( (Map<String, Object>) value, mapper ) );
            }
            else if ( value instanceof Iterable )
            {
                params.put( name, serialize( (Iterable) value, mapper ) );
            }
            else
            {
                throw new IllegalArgumentException( "Could not serialize value " + entry.getKey() + " " + entry
                        .getValue() );
            }
        }
        return params;
    }

    static ArrayNode serialize( Iterable<Object> iterable, ObjectMapper mapper )
    {
        ArrayNode array = mapper.createArrayNode();
        for ( Object value : iterable )
        {
            if ( value == null )
            {
                array.addNull();
            }
            else if ( value instanceof String )
            {
                array.add( value.toString() );
            }
            else if ( value instanceof Integer )
            {
                array.add( (Integer) value );
            }
            else if ( value instanceof Long )
            {
                array.add( (Long) value );
            }
            else if ( value instanceof Boolean )
            {
                array.add( (Boolean) value );
            }
            else if ( value instanceof BigDecimal )
            {
                array.add( (BigDecimal) value );
            }
            else if ( value instanceof Double )
            {
                array.add( (Double) value );
            }
            else if ( value instanceof byte[] )
            {
                array.add( (byte[]) value );
            }
            else if ( value instanceof Float )
            {
                array.add( (Float) value );
            }
            else if ( value instanceof Number )
            {
                final Number number = (Number) value;
                if ( number.longValue() == number.doubleValue() )
                {
                    array.add( number.longValue() );
                }
                else
                {
                    array.add( number.doubleValue() );
                }
            }
            else if ( value instanceof Map )
            {
                array.add( serialize( (Map<String, Object>) value, mapper ) );
            }
            else if ( value instanceof Iterable )
            {
                array.add( serialize( (Iterable) value, mapper ) );
            }
            else
            {
                throw new IllegalArgumentException( "Could not serialize value " + value );
            }
        }
        return array;
    }
    
	/**
	 * Prepares a query for embedding within the JSON body.
	 * 
	 * <ol>
	 *    <li>Normalizes cypher property value delimiters to single quotes.</li>
	 *    <li>Escapes unescaped double quotes found within single quote delimiters</li>
	 *    <li>Replaces newline characters with spaces.</li>
	 * </ol> 
	 * 
	 * @param query the unescaped query
	 * @return the escaped query
	 */
    static String escapeQuery( String query )
    {
    	StringBuilder buf = new StringBuilder(query);
    	boolean inSingleQuotes = false;
    	boolean coerce = false;
    	for(int i = 0; i < buf.length(); i++) {
    		if(buf.charAt(i) == '\'' && (i > 0 && buf.charAt(i - 1) != '\\'))
    			inSingleQuotes = !inSingleQuotes;
    		else if(buf.charAt(i) == '\"') {
    			if(!inSingleQuotes || (coerce && i > 0 && buf.charAt(i - 1) != '\\')) {
    				buf.setCharAt(i, '\'');
        			inSingleQuotes = !inSingleQuotes;
        			coerce = inSingleQuotes;
    			}
    			else if(i > 0 && buf.charAt(i - 1) != '\\')
    				buf.insert(i, '\\');
    		}
    		else if(buf.charAt(i) == '\n')
    			buf.setCharAt(i, ' ');
    	}

    	return buf.toString();
    }
	
    
}
