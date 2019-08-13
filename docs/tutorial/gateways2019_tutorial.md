# Gateways 2019 Tutorial

Objective: learn the basics of the Apache Airavata Django Portal and how to make
both simple and complex customizations to the user interface.

Prerequisites: tutorial attendees should have:

- a laptop on which to write Python code
- Git client

We'll install Python and Node.js as part of the tutorial.

## Outline

- Introduction
- Presentation: Overview of Airavata and Django Portal
  - History of the Airavata UI and how did we get here
- Hands on: run a basic computational experiment in the Django portal
- Tutorial exercise: customize the input user interface for an application
- (Optional) Tutorial exercise: Create a custom web component to customize the
  input interface
- Tutorial exercise: Create a custom output viewer for an output file
- Tutorial exercise: Create a custom Django app
  - use the `AiravataAPI` JavaScript library for utilizing the backend Airavata
    API
  - develop a simple custom user interface for setting up and visualizing
    computational experiments

## Hands on: run a Gaussian computational experiment in the Django portal

### Create a portal user account

First, you'll need a user account. Go to the
[Create Account](https://pearc19.scigap.org/auth/create-account) page and select
**Sign in with existing institution credentials**. This will take you to the
CILogon institution selection page. If you don't find your institution listed
here, go back to the **Create Account** page and fill out the form to create an
account with a username, password, etc.

After you've logged in, an administrator can grant you access to run the
Gaussian application. During the tutorial we'll grant you access right away and
let you know.

When you log in for the first time you will see a list of applications that are
available in this science gateway. Applications that you are not able to run are
greyed out but the other ones you can run. Once you are granted access, refresh
the page and you should now see that you the **Gaussian** application is not
greyed out.

### Submit a test job
