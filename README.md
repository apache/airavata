
# Developer notes

## Allow insecure OAuth callbacks

For local development, [set the OAUTHLIB_INSECURE_TRANSPORT environment variable
to allow insecure OAuth
callbacks](http://requests-oauthlib.readthedocs.io/en/latest/examples/real_world_example.html)
before starting the server:

```
export OAUTHLIB_INSECURE_TRANSPORT=1
```

## USING CMS

#### Logging in to CMS Dashboard

1. Make sure you are on the homepage of the website. Now, append `/cms` to the url and click ENTER/GO
2. You will be redirected to a Login Interface for Wagtail which looks like shown below:

![wagtail login](https://github.com/stephenpaul2727/airavata-django-portal/blob/cms/docimages/wagtail-login.png)


3. Input your Username and Password and click `Sign In`. If you are authorized by gateway admin to access the CMS dashboard, you will be redirected to the Wagtail dashboard which should look like shown below:

![wagtail dashboard](https://github.com/stephenpaul2727/airavata-django-portal/blob/cms/docimages/wagtail-dashboard.png)


#### CMS Dashboard Overview

![wagtail dashboard overview](https://github.com/stephenpaul2727/airavata-django-portal/blob/cms/docimages/wagtail-dashboard-overview.png)


#### Page Structure in CMS

1. Pages in the CMS are created as a Tree Structure. 
2. You can have child pages to a particular page and child pages again to any of those child pages and so on. 
3. If you are using a pre-configured site, you don't need to set up any root page. Otherwise, if you are creating a website from scratch, you need to create a default root page like Home page which will be accessible at '/' url. you have to create it as a child of root page and configure site settings to set it as default page. 
4. If you have a pre-configured site and you just need to maintain it, you don't need to touch site settings unless your host name or site name changes.
5. The following diagram clearly depicts all the useful points to note about CMS page structure.

![CMS Page Structure](https://github.com/stephenpaul2727/airavata-django-portal/blob/cms/docimages/page-structure.png)


#### Creating a New Page

1. If you are building a site from scratch, the first page you create will be a child page of Root page and you should name it home page. Root page shouldn't have any other children. 
2. All the other pages you create from now on will be children of the home page or children of children of home page etc. 
3. If you want to create a page as a child page of Home page. Go to the dark side bar and find Pages Link. Click it.
4. You will get a side pop up which will contain "**Welcome to your new Wagtail Site**" and a "**pencil**" icon and a "**right-arrow**" icon. 
5. If you have a home page configured, then only you will find the "right-arrow" icon. Click on "right-arrow"
6. You will get "**Home**" Link. This is the default page. If you want to modify it you can click on "**pencil**" icon. if you have any child pages to "Home". you will also get a "**right-arrow**" icon.
7. To create a new page as a child of "Home". you need to click on "Home" Link.
8. Then Click on "**ADD CHILD PAGE**" link. 
9. Choose a page template type. Currently, three of them are available. 
  * Blank Page -> (Build a page from scratch (Best Way))
  * Cybergateway Home Page -> (Build an IU Themed Home Page)
  * Home Page -> (Build a seagrid website based Home page)
10. You can explore other page templates. But Blank Page is the best way to go as it enables you to build your website from scratch without mocking any other website theme.
11. Click on "Blank Page".
12. Each page has four tabs namely:
  * CONTENT (On page load)
  * CUSTOMIZATION
  * PROMOTE
  * SETTINGS
13. Provide a title(**required**) for the page so that you can see it in the CMS.
14. Head over to the **PROMOTE** Section. 
15. Provide a slug url ( which is the url extension at which the page will be available). for example if you are creating a documentation page. Provide a slug like `documentation` so that when you visit `<--yourwebsite.com-->/documentation` you will visit this page.
16. Provide a Page Title. This will show up in the page title of each page.
17. Please tick the **Show in Menu** to make the page appear in the Navbar Menu. This only works for children of **Home** This doesn't work for children of other pages.
18. Now head back to the **CONTENT** Section where you have already provided a Title. 
19. Click on "**+**" beside Row. 
20. It will open up a **PANEL** which contains all the items you can use to build an awesome website. Please click this [https://github.com/stephenpaul2727][Link] to see which one to choose for which purpose.
21. Every page follows a "**Bootstrap Grid Layout**" to make the website design process easy. [https://getbootstrap.com/docs/4.0/layout/grid/][Learn More here].
22. To understand this layout in laymens terms, This layout will divide each page into rows and columns like a Table. Columns are limited to 12. You can add as many rows as you can. 
23. This layout enables you to place items on panel correctly on the website. Please go through the link mentioned above. It is a good read.
24. Initially after clicking "**+**". it will open up one row. you can add as many items in that row as you can by clicking small grey "+" circle shaped icons on the top and bottom of each item.
25. To differentiate each item on each row. You will provide a unique attribute to **Custom class** textbox. for eg. (col-md-3). This will make the item take 3 columns space out of 12 available for a row. Similary add another item and give its **Custom class** (for eg. col-md-9). This will make item take 9 columns. Now you have used 12 columns in a row. 
26. Then you will click on "Add Row" which will make an another row. A similar song and dance takes place for this row as well.
27. After you have completed making all the rows for the page, you click "**Save Draft**" to save the page. 
28. Now your page has been created in CMS. but it is still in CMS and is not published yet.
29. To publish the page live. click on "**up-arrow**" next to "**Save Draft**". This will pop up a list.
30. Click on the "**Publish**" to make the page available to the entire world. 
31. Before you publish please make sure all the content is appropriately displayed or not. 
32. You can preview the page before you publish it by clicking "**Preview**" next to "**Save Draft**". This lets you get confident about how the page will be displayed. This is also a best practice for a content editor.
33. After you click "**Publish**". you will be redirected to the Parent Page dashboard if there are no validation errors.
34. You can now see the newly created page in the list. Click on "**Live**" or "**View Live**" button to visit the newly published page.
33. Congratulations! you have created a page using CMS.

#### UNDERSTANDING PANEL




 
  




#### Deleting a Page



#### Modifying a Page content

