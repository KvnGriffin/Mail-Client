/* SMtpClient.java 
 * Kevin Griffin 
 * Email: kevingriffin79@gmail.com
 * Program sends emails by bouncing them off a relay server, messages may have the body of text encrypted
 * Address book is contained in a text file called Contacts.txt.
 * Initial mail menu is started from the constructor in main.
 * 
 * File Menu:
 *            >> Choose File > Log On, enter address of smtp server and press OK.
 *            >> Log Off sends the QUIT command to the server
 *            >> Exit closes the socket and then calls System.exit(0) to terminate the program.
 * Mail Menu:
 *            >> New Mail, mail window pops up.
 *            >> You may the add addresses either manually into field or press "To" button to choose one.
 *            >> Discard will close the screen and send nothing
 *            >> Encrypt check box class the encryption method if ticked but only on the body, I could have 
 *               called it on the subject choose not to.
 *            >> Send, sends the mail and displays all the relevant smtp commands back from the server to
 *               the message Area.
 * Contacts:  
 *            >> lets you either add or view the contacts in the message area.
 *            >> Note: contacts.txt is not included.
 *   
 *  Replace used on linux as \n is newline not \r\n as Windows
 * and we need to terminate every new-line for smtp with \r\n. 
 *  
 * Note: application is set up specifically for UCD's mail server, public ISP mail relay servers may require
 *       changes to the code to handle specific server's responses.
 **/

import java.io.*;
import java.net.*; 
import javax.swing.*;
import java.awt.BorderLayout;
import java.util.*;	
import javax.swing.border.Border;
import javax.swing.border.SoftBevelBorder;
import javax.swing.BorderFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.BadLocationException;

 
public class SMtpClient { 
	 
	 private final int PORT = 25; // hard code the port number to smtp
	 private String address = "";
	 private Socket socket;  
	 private PrintWriter out;
	 private BufferedReader in;
	 JTextArea messageArea;
	 private boolean encryption = false; // used to turn on/off the encryption
	 private String myAddress = "97580465@ucdconnect.ie"; // hard coded in but code have made it a submittable field
	 ArrayList <String> contacts = new ArrayList<String>(); // Arraylist to hold the list of contacts
	 FileWriter fout;
	 FileReader fin;
	 
	SMtpClient() {
		 
		JFrame frame = new JFrame("SMTP");
        JPanel panel1 = new JPanel(new BorderLayout());
        JMenuBar menuBar = new JMenuBar();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,500);
        frame.setJMenuBar(menuBar);
        menuBar.add(createFileMenu());
        menuBar.add(createMailMenu());
        menuBar.add(createAddressBookMenu());
        messageArea = new JTextArea(20,25);
        panel1.add(messageArea);
        panel1.add(new JScrollPane(messageArea),"Center");
        panel1.setBorder(BorderFactory.createLineBorder(Color.lightGray, 15));
        frame.getContentPane().add(panel1, BorderLayout.CENTER); // Adds Button to content pane of frame
        frame.setVisible(true);
         contacts.add("");	 
    } // end constructor SMTPClient
    
    
    
    
    public JMenu createFileMenu() {  // creates the file menu for use in GUI
		    JMenu menu = new JMenu("File");
		    menu.add(createLogOnItem());  // add LogOn field
		    menu.add(createLogOffItem()); // add LogOff field
		    menu.add(createFileExitItem()); // add exit field
		return menu;
	} // end createFileMenu()
		
		
		
    public JMenuItem createLogOnItem()  {
		JMenuItem item = new JMenuItem("Log On");
		    class MenuItemListener implements ActionListener {
			    public void actionPerformed (ActionEvent event) {
				// show input dialog below sets the info on the pop up window
			    address = JOptionPane.showInputDialog(null,"Smtp server address:","Log On",JOptionPane.INFORMATION_MESSAGE);
			    try {
				    InetAddress addr = InetAddress.getByName(address); // is server address to connect to
	                messageArea.append("Connecting... \n");
	                socket = new Socket(addr, PORT);
                    try { 
		                 out = new PrintWriter(socket.getOutputStream(), true);
	                     in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		                }   catch (IOException er) {
					           System.out.println(er);
		                        }  
	                
	                        String recv = in.readLine();
	                        messageArea.append(recv +"\n");
	                        messageArea.append("HELO ucd.ie \n"); 
	                        String helo = "HELO ucd.ie \r\n";
	                        out.println(helo);  // send HELO command
	     
	                    String recved = in.readLine();
	                    messageArea.append(recved +"\n");
			    } catch (IOException er) {
					 System.out.println(er);
		              }  
			    } // end actionPerformed
		    } // end MenuItemListener
		ActionListener listener = new MenuItemListener();
		item.addActionListener(listener); // add listener to the menu 'item'
		return item;
	} // end createLogOnItem
	
	
	public JMenuItem createLogOffItem() {
		JMenuItem item = new JMenuItem("Log Off");
	    class MenuItemListener implements ActionListener {
			public void actionPerformed (ActionEvent event) {
			    String received = null;
			    try {
	                   out.println("QUIT \r\n");  // send the QUIT command
			           messageArea.append("QUIT \n");
	                   received = in.readLine();
	                   socket.close(); // close the socket
	                } catch (IOException er) {
					       System.out.println(er);
		                } 
						messageArea.append(received+"\n");
		    } // end actionPerformed
		} // end MenuItemListener
		ActionListener listener = new MenuItemListener();
		item.addActionListener(listener);
	    return item;
	}

    public JMenuItem createFileExitItem() {     // to close the program
	    JMenuItem item = new JMenuItem("Exit");
	    class MenuItemListener implements ActionListener {
			public void actionPerformed (ActionEvent event) {
				try {
				    socket.close(); // close the socket on exit
			             } catch (IOException er) {
					          System.out.println(er);
		                    }  
	                System.exit(0);       // close the program by calling System.exit(0)
			} // end actionPerformed
		} // end MenuItemListener
		ActionListener listener = new MenuItemListener();
		item.addActionListener(listener);
	    return item;
	}	
	
	/********** Mail menu ************/
    public JMenu createMailMenu() {
		    JMenu menu = new JMenu("Mail");
		    menu.add(createMailItem());
		return menu;
	}
	
	public JMenuItem createMailItem() {
		JMenuItem item = new JMenuItem("New mail");
		   	class MenuItemListener implements ActionListener {
			public void actionPerformed (ActionEvent event) {
				// show input dialog below sets the info on the pop up window
			   final JFrame frame2 = new JFrame("Mail Message"); // accessed from within inner class
               JPanel panel1 = new JPanel();  //(new BorderLayout());
               JPanel panel2 = new JPanel();
               JPanel panel3 = new JPanel();
               JPanel panel4 = new JPanel();
               frame2.setSize(700,700);
               
               JButton send = new JButton("Send");
               JButton discard = new JButton("Discard");
               
                final JCheckBox encrypt = new JCheckBox("Encrypt");
               encrypt.setSelected(false);
               encrypt.setMnemonic(KeyEvent.VK_G);
               panel1.add(send);
               panel1.add(discard);
               panel1.add(encrypt);
               
               final JTextField addr = new JTextField(50);  // text field for address
               JButton to = new JButton("To");
               panel2.add(to);
               panel2.add(addr);
               JLabel space = new JLabel("              "); // sorry had to use space to align fields
               JLabel space2 = new JLabel("              "); // due to layout being used
               JLabel subject = new JLabel("Subject:");
              
               final JTextField subj = new JTextField(48);
               panel2.add(space);
               panel2.add(subject);
               panel2.add(subj);
               panel2.add(space2);
               
               
               final JTextArea messageArea2 = new JTextArea(35,55);
               panel4.add(new JScrollPane(messageArea2));           
               frame2.getContentPane().add(panel1,BorderLayout.NORTH);
               frame2.getContentPane().add(panel2,BorderLayout.CENTER);
               frame2.getContentPane().add(panel4,BorderLayout.SOUTH);
               frame2.setVisible(true);
			
			/*********** Listeners for different buttons *****************/
			
			encrypt.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					encryption = false;
					 encryption = (e.getStateChange() == ItemEvent.SELECTED); // set encryption to true if selected
					 if(encryption == true)
					 System.out.println("Text will be encrypted"); // test to see if selection worked
 				}
			});
		    
		  
			// send button
			send.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent e) 
					{
						/********* Main meat of programme handles all coms with server after log on  *******/
						 
						out.println("MAIL FROM: "+myAddress + "\r\n");
	                    messageArea.append("MAIL FROM: "+myAddress + "\n");
	                    String received = null;
	                    try {
	                          received = in.readLine();
	                        } catch (IOException er) {
					            System.out.println(er);
		                        }  
	                    messageArea.append(received+"\n");
	                    
	                    String outGoingAddress = null;
						outGoingAddress = addr.getText();
						String []multipleAddresses = outGoingAddress.split("\\,");
	                    for( String s : multipleAddresses)
	                    {
						out.println("RCPT TO: "+ s +"\r\n");
						messageArea.append("RCPT TO: "+ s + "\n");
					}
						try {
	                          received = in.readLine();
	                        } catch (IOException er) {
				            	System.out.println(er);
		                         } 
						messageArea.append(received+"\n");
						
						out.println("DATA \r\n");
						messageArea.append("DATA" +"\n");
						try {
	                         received = in.readLine();
	                        } catch (IOException er) {
					            System.out.println(er);
		                        } 
						messageArea.append(received + "\n");
						
				        int length = 0;
					    length = messageArea2.getDocument().getLength();
					    String body = null;
					    try {
							/* replace used on linux as \n is newline not \r\n as Windows
							   and we need to terminate every new-line for smtp with
							   \r\n    */
					            body = messageArea2.getDocument().getText(0,length).replace("\n","\r\n");
					    
					            System.out.println(body);
					        } catch (Exception ess) {
					            System.out.println(ess);
				                }
				    
					        if(encryption == true) { 
				     		    body = caeserCipher(body); // only choose to encrypt the text body of mail
						    }
						    encryption = false; // reset to false
                            out.println("Subject:"+ subj.getText() + "\r\n" + body + "\r\n.\r\n"); // full message
						messageArea.append("Subject:"+subj.getText() + "\n" + body + "\n");
			            frame2.setVisible(false);
					}
		    });   // end of send button ActionListener
				
				
			// discard button
			discard.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
						frame2.setVisible(false);
			    }
			});  // end of discard button ActionListener
				
			// To button
			to.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
						JPanel pan1 = new JPanel();
						JPanel pan2 = new JPanel();
						final JFrame boxFrame = new JFrame("Contacts");
						// ArrayList 'contacts' added to JComboBox below as an array.
						try {
						    fin = new FileReader("Contacts.txt");
						    BufferedReader input = new BufferedReader(fin);
						    String line = null;
						        while((line = input.readLine()) != null)
						        {
									if(!contacts.contains(line))  // if it's not already in the arrayList
							       contacts.add(line); // add each contact from file to arrayList
						        }
						    } catch (IOException es) {
					                  System.err.println("Unable to read from file");   
				                    }
						
						JComboBox list = new JComboBox(contacts.toArray());  // filled array is now added to combo box
						JButton ok = new JButton("OK");
						pan1.add(list);
						pan2.add(ok);
						boxFrame.getContentPane().add(pan1,BorderLayout.NORTH);
						boxFrame.getContentPane().add(pan2,BorderLayout.SOUTH);
					    boxFrame.setVisible(true);
						boxFrame.setSize(300,200);
						
			// Ok button			
			ok.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent e) {  
						boxFrame.setVisible(false);
			    }
		    });
			// combo box to display contacts			
			list.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {         
						JComboBox cb = (JComboBox)e.getSource();
						String email = (String)cb.getSelectedItem(); // email is selected email from list
						String emailAlreadyAdded = addr.getText(); 
						if (emailAlreadyAdded.trim().equals("")) // check to see addr field is empty
						{
							addr.setText(email); // sets the addr field to the email address selected, if it's empty
						}
						/* else sets the addr field to the email address selected + the one that is already in it.
						 * this allows for multiple recipients to be selected */
						else {
						 addr.setText( emailAlreadyAdded + "," + email);
						}
					  
			        }
			    }); // end list combo box listener
		    }
		});
				
				} // end actionPerformed
		} // end MenuItemListener
		ActionListener listener = new MenuItemListener();
		item.addActionListener(listener); // add listener to the menu 'item'
		return item;
	} // end createLogOnItem
	
	
	/********* Address Book Menu ***************/
	    public JMenu createAddressBookMenu() {
		    JMenu menu = new JMenu("Contacts");  // create menu
		    menu.add(createAddressBookAddItem()); // add - addItem
		    menu.add(createAddressBookViewItem()); // add - ViewItem
		  return menu;
	    }
	
	
		public JMenuItem createAddressBookViewItem() {
		JMenuItem item = new JMenuItem("View");
		    class MenuItemListener implements ActionListener {
		        public void actionPerformed (ActionEvent event) {
			        try {
						fin = new FileReader("Contacts.txt");
						BufferedReader input = new BufferedReader(fin);
						String line = null;
						    while((line = input.readLine()) != null) // read in all contacts one line at a time
						    {
								messageArea.append(line + "\n"); // display in message Area
						    }
						}   catch (IOException es) {
					           System.err.println("Unable to read from file");   
				            }
				} // end actionPerformed
		} // end MenuItemListener
		    ActionListener listener = new MenuItemListener();
		    item.addActionListener(listener); // add listener to the menu 'item'
		return item;
	} // end createLogOnItem
	
	@SuppressWarnings("unchecked")
	public JMenuItem createAddressBookAddItem() {
		JMenuItem item = new JMenuItem("Add");
		class MenuItemListener implements ActionListener {
		public void actionPerformed (ActionEvent event) {
				String contact = null;
			   	contact= JOptionPane.showInputDialog(null,"New Contact:","Contact",JOptionPane.INFORMATION_MESSAGE);		
			   	try {
        	        fout = new FileWriter("Contacts.txt", true);
            	    } catch (IOException e) {
					        System.err.println("Unable to write to file");   
				            }	
 				try {
					fout.write(contact + "\n"); // write to file, newline is \n on linux
					fout.close(); // close file
				} catch (IOException e) {
					    System.err.println("Unable to write to file");   
				        }	
				messageArea.append(contact + "\n"); // show on screen for testing purposes
			} // end actionPerformed
		} // end MenuItemListener
		ActionListener listener = new MenuItemListener();
		item.addActionListener(listener); // add listener to the menu 'item'
		return item;
	} // end createLogOnItem
		
	/* Simple encryption method */ 
	public String caeserCipher(String input) {
	    char[] chars = input.toCharArray(); // convert input string to char array
	       for (int i=0; i < input.length(); i++) 
            {
               char ch = chars[i];
               if (ch >= 32 && ch <= 127) 
               {
                      int x = ch - 32;
                      x = (x + 3) % 96; // move 3 places
                    if (x < 0) 
                      x += 96; 
                      chars[i] = (char) (x + 32); // cast back to char from int
                }
            }
        return new String(chars);  // return shifted string
	}
	 
	 

	 
	public static void main (String[] args) throws Exception {
	      
	      SMtpClient mail = new SMtpClient(); // create a new instance of mail client
	
	} // end of main
	 
} // end of class  SMtpClient




