# EJBCA - Dynamic Criticality Custom Extension Plugin

It allows to customize extension criticality through EJBCA WS API.

## Installation and usage

**First check the latest EJBCA version that this extension was tested with in the comments at `build.xml`.**

Then, copy the `blobfish-ejbca-dynamic-criticality-custom-extension-plugin/` folder to any location outside the EJBCA source code directory, for example:

```
$ sudo mkdir /opt/misc/
$ sudo cp -r ../path/to/blobfish-ejbca-dynamic-criticality-custom-extension-plugin/ /opt/misc/
```

Now:

```
$ sudo mkdir /opt/ejbca-custom/conf/plugins/
$ sudo cp /opt/misc/blobfish-ejbca-dynamic-criticality-custom-extension-plugin/dynamiccriticalitycustomextensionplugin.properties.sample /opt/ejbca-custom/conf/plugins/dynamiccriticalitycustomextensionplugin.properties
```

And then edit `/opt/ejbca-custom/conf/plugins/dynamiccriticalitycustomextensionplugin.properties`, where the property `plugin.ejbca.ant.file` should point to the place where the plugin's `build.xml` is located, for example:

```
plugin.ejbca.ant.file /opt/misc/blobfish-ejbca-dynamic-criticality-custom-extension-plugin/build.xml
```

Note that the previous is already done if the plugin folder has been copied to the suggested `/opt/misc` directory.

Then redeploy EJBCA:

```
$ cd /opt/ejbca
$ sudo -E ant build deployear
```

Now, the custom extensions can be configured from the Admin GUI, section “System Configuration > Custom Certificate Extensions”:

* KU
  * Object Identifier (OID): 2.5.29.15
  * Label: My Key Usage
  * Extension Class: Dynamic Criticality Certificate Extension
  * Critical: Active
  * Required: Active
  * Dynamic: true
  * Encoding: RAW

* EKU
  * Object Identifier (OID): 2.5.29.37
  * Label: My Extended Key Usage
  * Extension Class: Dynamic Criticality Certificate Extension
  * Critical: Disabled
  * Required: Active
  * Dynamic: true
  * Encoding: RAW

## TODOS
- Determine if the need for this plugin could be avoided by using something like "Custom Certificate Extension Data" (https://localhost:8443/ejbca/doc/End_Entity_Profiles_Fields.html#src-41288512_id-.EndEntityProfilesFieldsv7.0.1-Custom_Certificate_Extension_Data) sent over the WS. But actually I think that "Custom Certificate Extension Data" is the current method to receive these extensions value currently. Take a look again into this.
