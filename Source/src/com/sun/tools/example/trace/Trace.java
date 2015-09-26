/*
 * @(#)Trace.java	1.5 03/12/19
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * Copyright (c) 1997-2001 by Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 * 
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */
package com.sun.tools.example.trace;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.connect.*;

import dapeng.DotCreator;

import java.util.Map;
import java.util.List;
import java.util.Iterator;

import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * This program traces the execution of another program.
 * See "java Trace -help".
 * It is a simple example of the use of the Java Debug Interface.
 *
 * @version     @(#) Trace.java 1.5 03/12/19 10:20:23
 * @author Robert Field
 */
public class Trace
	{

		// Running remote VM
		private final VirtualMachine	vm;

		// Thread transferring remote error stream to our error stream
		private Thread					errThread		= null;

		// Thread transferring remote output stream to our output stream
		private Thread					outThread		= null;

		// Mode for tracing the Trace program (default= 0 off)
		private int						debugTraceMode	= 0;

		//  Do we want to watch assignments to fields
		private static boolean			watchFields		= false;

		// Class patterns for which we don't want events
		private String[]				excludes		= { "java.*", "javax.*", "sun.*", "com.sun.*", "org.eclipse.jface.text.reconciler.*", 
				"org.eclipse.swt.internal.win32.*" };

		// Control whether to trace
		public static boolean			isTracing		= false;

		public static JFrame			controlPanel;

		public static JButton			jbIsTracing		= new JButton ( isTracing ? "Stop Tracing" : "Start Tracing");//"tracing=" + isTracing );

		//control wheter to print \ and / to simulate in/out call
		public static boolean			isIndented		= false;

		public static DotCreator		dotCreator		= null;

		private static EventThread		eventThread;

		/**
		 * main
		 */
		public static void main ( String[] args )
		{

			//create a frame to setup isTracing
			controlPanel = new JFrame ( );
			controlPanel.setSize ( 300, 300 );
			controlPanel.setLayout ( new FlowLayout ( ) );
			jbIsTracing.setSize ( 300, 300 );
			jbIsTracing.addActionListener ( new ActionListener ( ) {
				public void actionPerformed ( ActionEvent event )
				{
					if ( isTracing )
						jbIsTracing.setText ( "Start Tracing" );
					else
						jbIsTracing.setText ( "Stop Tracing" );
					isTracing = !isTracing;
					eventThread.enableEventRequests ( isTracing );
				}
			} );
			controlPanel.add ( jbIsTracing );
			controlPanel.pack ( );
			controlPanel.setVisible ( true );
			controlPanel.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // exit if control window is closed

			long startTime = System.currentTimeMillis ( );
			System.out.println ( "starting..." );
			new Trace ( args );
			long duration = System.currentTimeMillis ( ) - startTime;
			System.out.println ( "----tracing-----duration: " + duration );

			controlPanel.dispose ( );

			if ( !EventThread.hasMultiThreadLogs )
				dotCreator.close ( );
		}

		/**
		 * Parse the command line arguments.  
		 * Launch target VM.
		 * Generate the trace.
		 */
		public Trace(String[] args)
		{				
			// If Trace is closed, close the target VM as well.
			Runnable runnable = new Runnable() {
			    public void run() {
			      vm.process().destroy();
			    }
			};
			Runtime.getRuntime().addShutdownHook(new Thread(runnable));
			
			// Listen for a "QUIT" message on stdin.
			Thread quitThread = new Thread() {
				public void run() {
					BufferedReader streamReader  = new BufferedReader(new InputStreamReader(System.in));  
					  
					try {
						while (true) {  
							if (streamReader.ready()) {
								String line = streamReader.readLine();
								if ((line != null) && (line.equals("QUIT")))
									System.exit(0);
								Thread.sleep(50);
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
				}
			};
			quitThread.setDaemon(true); // don't let this thread keep the program running
			quitThread.start();
			
			
			// Ok, now we can parse the arguments
			PrintWriter writer = new PrintWriter ( System.out );
			String dotdir = "";
			// no need to create a global slice file for multi threads
			if ( !EventThread.hasMultiThreadLogs )
				dotCreator = new DotCreator ( );
			int inx;
			for ( inx = 0; inx < args.length; ++inx )
			{
				String arg = args[inx];
				if ( arg.charAt ( 0 ) != '-' )
				{
					break;
				}
				if ( arg.equals ( "-output" ) )
				{
					try
					{
						writer = new PrintWriter ( new BufferedWriter( new FileWriter ( args[++inx] ) ) );
					} catch ( IOException exc )
					{
						System.err.println ( "Cannot open output file: " + args[inx] + " - " + exc );
						System.exit ( 1 );
					}
				} else if ( arg.equals ( "-dotdir" ) )
				{
					dotdir = args[++inx];
					if (dotCreator != null) dotCreator.dotdir = dotdir;
				} else if ( arg.equals ( "-all" ) )
				{
					excludes = new String[0];
				} else if ( arg.equals ( "-fields" ) )
				{
					watchFields = true;
				} else if ( arg.equals ( "-dbgtrace" ) )
				{
					debugTraceMode = Integer.parseInt ( args[++inx] );
				} else if ( arg.equals ( "-help" ) )
				{
					usage ( );
					System.exit ( 0 );
				} else
				{
					System.err.println ( "No option: " + arg );
					usage ( );
					System.exit ( 1 );
				}
			}
			if ( inx >= args.length )
			{
				System.err.println ( "<class> missing" );
				usage ( );
				System.exit ( 1 );
			}
			StringBuffer sb = new StringBuffer ( );
			sb.append ( args[inx] );
			for ( ++inx; inx < args.length; ++inx )
			{
				sb.append ( ' ' );
				sb.append ( args[inx] );
				//System.out.println( args[inx] );
			}
			vm = launchTarget ( sb.toString ( ) );
			generateTrace ( writer, dotdir );
		}

		/**
		 * Generate the trace.
		 * Enable events, start thread to display events, 
		 * start threads to forward remote error and output streams,
		 * resume the remote VM, wait for the final event, and shutdown.
		 */
		void generateTrace ( PrintWriter writer, String dotdir )
		{
			vm.setDebugTraceMode ( debugTraceMode );
			eventThread = new EventThread ( vm, excludes, writer, dotdir );
			eventThread.setEventRequests ( watchFields );
			eventThread.enableEventRequests ( isTracing );
			eventThread.start ( );
			redirectOutput ( );
			vm.resume ( );

			// Shutdown begins when event thread terminates
			try
			{
				eventThread.join ( );
				errThread.join ( ); // Make sure output is forwarded 
				outThread.join ( ); // before we exit
			} catch ( InterruptedException exc )
			{
				// we don't interrupt
			}
			writer.close ( );
			// if (eventThread.hasMultiThreadLogs)
			eventThread.closeAllLogWriters ( );
			eventThread.closeAllDotCreators ( );
		}

		/**
		 * Launch target VM.
		 * Forward target's output and error.
		 */
		VirtualMachine launchTarget ( String mainArgs )
		{
			LaunchingConnector connector = findLaunchingConnector ( );
			Map arguments = connectorArguments ( connector, mainArgs );
			try
			{
				return connector.launch ( arguments );
			} catch ( IOException exc )
			{
				throw new Error ( "Unable to launch target VM: " + exc );
			} catch ( IllegalConnectorArgumentsException exc )
			{
				throw new Error ( "Internal error: " + exc );
			} catch ( VMStartException exc )
			{
				exc.printStackTrace ( );
				System.out.println ( "\nDapeng:\n" + exc.getCause ( ) + "\ngetCause()" );
				throw new Error ( "Target VM failed to initialize: " + exc.getMessage ( ) );
			}
		}

		void redirectOutput ( )
		{
			Process process = vm.process ( );

			// Copy target's output and error to our output and error.
			errThread = new StreamRedirectThread ( "error reader", process.getErrorStream ( ), System.err );
			outThread = new StreamRedirectThread ( "output reader", process.getInputStream ( ), System.out );
			errThread.start ( );
			outThread.start ( );
		}

		/**
		 * Find a com.sun.jdi.CommandLineLaunch connector
		 */
		LaunchingConnector findLaunchingConnector ( )
		{
			List connectors = Bootstrap.virtualMachineManager ( ).allConnectors ( );
			Iterator iter = connectors.iterator ( );
			while ( iter.hasNext ( ) )
			{
				Connector connector = ( Connector ) iter.next ( );
				if ( connector.name ( ).equals ( "com.sun.jdi.CommandLineLaunch" ) )
				{
					return ( LaunchingConnector ) connector;
				}
			}
			throw new Error ( "No launching connector" );
		}

		/**
		 * Return the launching connector's arguments.
		 */
		Map connectorArguments ( LaunchingConnector connector, String mainArgs )
		{
			Map arguments = connector.defaultArguments ( );

			//        System.out.println( arguments.toString() );
			Connector.Argument options = ( Connector.Argument ) arguments.get ( "options" );
			//options.setValue( "-jar startup.jar" );

			Connector.Argument mainArg = ( Connector.Argument ) arguments.get ( "main" );
			if ( mainArg == null )
			{
				throw new Error ( "Bad launching connector" );
			}
			mainArg.setValue ( mainArgs );

			if ( watchFields )
			{
				// We need a VM that supports watchpoints
				Connector.Argument optionArg = ( Connector.Argument ) arguments.get ( "options" );
				if ( optionArg == null )
				{
					throw new Error ( "Bad launching connector" );
				}
				optionArg.setValue ( "-classic" );
			}
			//    System.out.println( arguments.toString() );
			return arguments;
		}

		/**
		 * Print command line usage help
		 */
		void usage ( )
		{
			System.err.println ( "Usage: java Trace <options> <class> <args>" );
			System.err.println ( "<options> are:" );
			System.err.println ( "  -output <filename>   Output trace to <filename>" );
			System.err.println ( "  -all                 Include system classes in output" );
			System.err.println ( "  -help                Print this help message" );
			System.err.println ( "<class> is the program to trace" );
			System.err.println ( "<args> are the arguments to <class>" );
		}
	}
