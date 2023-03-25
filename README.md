<a name="readme-top"></a>

<!-- PROJECT SHIELDS -->
<!--
*** markdown "reference style" links 
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <!--<a href="">
    <img src="images/logo.png" alt="Logo" width="80" height="80">
  </a>-->

  <h1 align="center">Building Scalable Distribution System Assignment 2</h1>

  <p align="center">
    Codes for completing Assignment 2
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

<!--[![Product Name Screen Shot][product-screenshot]](https://example.com)-->

This project deploy a servlet server on Tomcat 9 on AWS EC2 instance. The server act as producer(publisher) when receiving incoming HTTP requests, connect to RabbitMQ hosted on another EC2 instance, with the consumers program running on the third EC2 instance. 
The client program provide a way to load test the whole server set up (servlet server, RabbitMQ server and the consumer).

<p align="right">(<a href="#readme-top">back to top</a>)</p>



### Built With

#### Development Environment
| Environment      | Description |
| ----------- | ----------- |
| Operating System      | Windows 10 Home Edition (for main code) and Ubuntu 22.04.1 LTS (to run the redis server)      |
| IDE                |Intellij Ultimate |
|Browser            | Chrome |
|Main language     | Java|

#### Main Tools 
| Main Tool      | Description of Usage |
| ----------- | ----------- |
| AWS Application Load Balancer | For testing the server with two instances |
| AWS | Cloud computing platform |
| RabbitMQ | Message Broker |
| Tomcat 9 | for deploying the server servlet |

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- GETTING STARTED -->
## Getting Started

You can run the backend on your local machine or deploy to AWS for testing. 

### Local Set Up Guide

1. Install and deploy RabbitMQ
2. Change the server address to localhost in the servlet and consumers program. 
3. Run the consumers program. 
4. Deploy the server program into Tomcat 9 on intellij
5. Change the server address in the client program to localhost:8080. 
6. Select any of the client configuration and run it. 

### AWS Set Up Guide

1. Install RabbitMQ on one EC2 instance (recommend to associate with an elastic IP address). 
2. Set up an admin user with password on the RabbitMQ. Make sure the admin user have full access to the RabbitMQ for 
declaring exchange, creating connection and channels, declaring queue etc. 
3. Change the server address of the servlet and consumers program to the IP address of EC2 instance (of the RabbitMQ). 
4. Add Username and password details. 
5. Package the consumers program into separate jar file. Upload it to one EC2 instance. And run both programs. 
6. Package the servlet into the standard war file. Upload it to the EC2 instance with Tomcat server installed. 
7. Change the server address in the client program to the IP address of EC2 instance with Tomcat server installed. 
8. Select any of the client configuration and run it.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- USAGE EXAMPLES -->
## Usage

### Useful commands
After deploying the consumer program as executable jar file into EC2 instance, you can specify 
1. The number of threads as first command-line argument provided when running the jar file.
2. The server address as second command-line argument provided when running the jar file. 
Example java -jar consumers-like.jar 20 172.22.22.135 will run the consumers-like.jar program with 20 threads and server address of 172.22.22.135
Note you cannot skip the first argument if you want to specify the server address using second argument. 
If you dont specify the arguments, the default value will be 10 threads and the server address you set up in the original program. 



<!-- ROADMAP -->
## TODO Roadmap

N/A.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE.txt` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- CONTACT -->
## Contact

My linkedin - [https://www.linkedin.com/in/shiang-jin-chin-b1575944/](https://www.linkedin.com/in/shiang-jin-chin-b1575944/)

Project Link: [https://github.com/sjchin88/SociusApp-backend](https://github.com/sjchin88/SociusApp-backend)

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- ACKNOWLEDGMENTS -->
## Acknowledgments

* [An awesome README template](https://github.com/othneildrew/Best-README-Template)
