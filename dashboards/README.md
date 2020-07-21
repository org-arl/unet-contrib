# Dashboards

This folder contains various web-based dashboards for Unet modems.

## Usage (< UnetStack v3.1.0)

Copy the contents of this folder to the `scripts` directory on your modem.

To enable the dashboards:
```
> dashboards
```

This will give you a list of dashboards installed. Control-clicking on
the relevant URL will open the dashboard.


## Usage (>= UnetStack v3.1.0)

Dashboards are automatically installed in UnetStack. They are accessible though the menu in the web interface or the shell command

```
> dashboards
```

## Customization

You can create and add your own Dashboard to UnetStack. Dashboards are simple `html` files, which are served by UnetStack. Any `html` files in the `scripts` directory of UnetStack will be served on the URL scheme `http://<unetstack-ip>/scripts/<dashboard-name>.html`

### Template

[DashboardTemplate.html](DashboardTemplate.html) is a template you can use to start creating your own Dashboard. Replace the commented out HTML, CSS and JavaScript section with your own code to create your own Dashboard.

Add your customized Dashboard to the `scripts` directory in UnetStack, and UnetStack will display your Dashboard on the next refresh of the Web interface.

### Available Libraries and Style-sheets

By default, UnetStack's web interface supports the following Stylesheets and JavaScript libraries

- `/js/unet.js` : unet.js - a helper library to access Unet functionality.
- `/fjage/fjage.js`: fjage.js - a WebSockets based JavaScript Gateway for [fjage](https://github.com/org-arl/fjage)
- `/js/webif.js`: webif.js - helper library to create UnetStack Web based User Interface.

The UI framework is based on [modular-admin](https://github.com/modularcode/modular-admin-html) which requires the following files.

- `/js/vendor/modular-admin/modular-admin.js` :
- `/css/vendor/modular-admin.css`
- `/css/vendor/font.css`

If any other Style-sheets or JavaScript libraries are required, they can be added to the `scripts` directory in UnetStack and they will be available at `/scripts/libname.js` to your Dashboard
