## OpenVPN PoC

This PoC allows clients to access private resources in a AWS VPC via a VPN connection.
The are 2 ways of making it possible:
- A managed VPN Connection built on top of an AWS Virtual Private Gateway
- Using your own VPN

The chosen approach was to provision an OpenVPN server instance following this [tutorial](https://medium.freecodecamp.org/how-you-can-use-openvpn-to-safely-access-private-aws-resources-f904cd24f890).

The basic network topology to integrate OpenVPN:

[Network topology]()