Edit code from https://github.com/PortSwigger/taborator.git

# Collab_Fixed
+ A Burp extension to show and choose FIXED Collaborator Client in a tab along with the number of interactions in the tab name.
+ This tools also auto logs records into a files 


# Installation and usage

- Import your config_collab.json file:
+ in config_collab.json you have to enter your "biid" (can use wireshark, tshark, tcpdump to get your biid from burp suite), your collaborator id, your CNAME for this collaborator id (optional)
 ![Client Screenshot](https://github.com/123Pro123Pro/Burp-Collab-Fixed/blob/main/images/config.png)
- Choose where you want to save logs records.

To use the extension right click in a repeater tab and choose Collab_Fixed->Insert Collaborator payload. This will create a Collaborator payload that is specific to the extension. You can also use Collab_Fixed->Insert Collaborator placeholder this will create a placeholder that is replaced with a Collaborator payload. The advantage of this is the original request will be stored and shown in the interactions.

Install from the BApp store.

# Screenshots

![Client Screenshot](https://github.com/123Pro123Pro/Burp-Collab-Fixed/blob/main/images/screenshot-client1.png)

![Tab Screenshot](https://github.com/123Pro123Pro/Burp-Collab-Fixed/blob/main/images/screenshot-tab.png)


