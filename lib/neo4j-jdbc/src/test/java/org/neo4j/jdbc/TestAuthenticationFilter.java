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
package org.neo4j.jdbc;


import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author mh
 * @since 03.12.12
 */
public class TestAuthenticationFilter implements Filter
{
    static String USER = "foo";
    static String PASSWORD = "bar";

    @Override
    public void init( final FilterConfig filterConfig ) throws ServletException
    {
    }

    public void doFilter( final ServletRequest req, final ServletResponse res, final FilterChain chain )
            throws ServletException, IOException
    {
        if ( !(req instanceof HttpServletRequest) || !(res instanceof HttpServletResponse) )
        {
            throw new ServletException( "request not allowed" );
        }

        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        final String header = request.getHeader( "Authorization" );

        if ( checkAuth( ((HttpServletRequest) req).getMethod(), header ) )
        {
            chain.doFilter( request, response );
        }
        else
        {
            sendAuthHeader( response );
        }
    }

    public void destroy()
    {
    }

    private boolean checkAuth( String method, String header ) throws IOException
    {
        if ( header == null )
        {
            return false;
        }

        final String encoded = header.substring( header.indexOf( " " ) + 1 );
        byte[] credentials = Base64.decodeBase64(encoded );
        final String credentialString = new String( credentials );
        return credentialString.equals( String.format( "%s:%s", USER, PASSWORD ) );
    }

    private void sendAuthHeader( HttpServletResponse response ) throws IOException
    {
        String realmName = "test.neo4j.org";
        response.setHeader( "WWW-Authenticate", "Basic realm=\"" + realmName + "\"" );
        response.sendError( HttpServletResponse.SC_UNAUTHORIZED );
    }
}
