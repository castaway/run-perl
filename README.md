RunPerl - A minimal Android app to start a Perl process on Android
==================================================================

Intro
-----

RunPerl is a very small Android application which will install a copy of Perl, then run the Perl script found in "uk.me.jandj.runPerl/runperl.pl". It includes a default runperl.pl script which will manipulate the UI of the main Activity, as a demonstration.

RunPerl includes, and extends, a copy of [Object-Remote-Java][orj].

RunPerl is a work in progress.

The RunPerl repo does not include a copy of Perl itself, there is a [downloadable copy of 5.17.4][downloadperl], or you can grab [the whole thing][runperldownload] compiled as a demo application.

For more docs, and a challenge, see: [Jess' blog post][blogpost], and the [Object-Remote-Java docs][ordocs]. 

The actual Perl binary is installed into /data/data/uk.me.jandj.runPerl/files/perl/perl. This can be run from the commandline using a terminal emulator or shell app.

HowTo
-----

### Build and deploy your own app using RunPerl ###

1. Create a main script for your application.

2. Copy the script to res/raw/runperl.pl in the checkout.

3. Update the res/raw/perl_libs.zip archive with any modules your application requires or own modules you have written. Make sure the modules are stored at the same directory level as the existing ones. For example My::Module should be /My/Module.pm in the zip file.

4. Download and unpack the Android SDK.

5. Create a local "local.properties" file for Ant, and add at least one line that contains "sdk.dir=XXX" in which XXX is the full path to the unpacked Android SDK.

6. Run "ant debug" to build the apk, then copy the resulting "bin/RunPerl-debug.apk" onto the device and run it. Ensure you first uninstall any previous RunPerl version otherwise the new libs will not be installed.

[orj]: https://github.com/theorbtwo/Object-Remote-Java
[runperldownload]: https://www.box.com/s/pbc1xcd6n88cykdomdh6
[blogpost]:  http://desert-island.me.uk/~castaway/blog/2012-12-perl-on-android-christmas-fun.html
[ordocs]: https://github.com/theorbtwo/Object-Remote-Java/blob/master/HOWTO.md
[downloadperl]: https://www.box.com/s/z10a2u6ca3ylpqdqm013

