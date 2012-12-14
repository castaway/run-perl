#!perl

## Non-Core modules, required for Object::Remote
use lib '/mnt/sdcard/uk.me.jandj.runPerl/extras/lib';

use strictures 1;
use Object::Remote;
use Time::HiRes 'sleep';

use Object::Remote::Connector::TCP;
my $conn = Object::Remote->connect('tcp://127.0.0.1:9849/');

my $activity = uk::me::desert_island::theorbtwo::bridge::AndroidServiceStash->can::on($conn, 'get_activity')->();

# onActivityResult IDs:
my $PICK_FILE_RESULT = 1;

my $edit_text = android::widget::EditText->new::on($conn, $activity);
$edit_text->setLines(20);
$edit_text->setWidth(300);
$edit_text->setText("This is Perl $^V\n If you have the OI File Manager (or another that implements the PICK_FILE intent) installed, then you can click the button and open a file. The code for this program is in uk.me.jandj.runPerl/runperl.pl\n Edit this file and re-run the RunPerl app to run your changed code.\n Debugging will end up in the Android logcat, try the app CatLog to read it.\n\n Challengt - add a Save button and make it work.\n");
my $button = android::widget::Button->new::on($conn, $activity);
$button->setText("Open File");
my $layout = android::widget::LinearLayout->new::on($conn, $activity);
$layout->addView($edit_text);
$layout->addView($button);

my $callbacks = java::util::HashMap->new::on($conn);

$callbacks->put("on_activity_result", sub {
  my ($request_code, $result_code, $intent) = @_;

  if($request_code == $PICK_FILE_RESULT) {
    my $file_uri = $intent->getData()->getPath();
    print "Got file: $file_uri\n";
    my $file = java::io::File->new::on($conn, $file_uri);
    my $scanner = java::util::Scanner->new::on($conn, $file);
    my $file_content = '';
    my $count=0;
    while($scanner->hasNextLine()) {
        $file_content .= $scanner->nextLine() . "\n";
        print $count++;
    }
    print "Read file: $file_content\n";
    my $update_runnable = uk::me::desert_island::theorbtwo::bridge::MethodCallRunnable->new::on($conn, $edit_text, 'setText', [$file_content]);
    $activity->runOnUiThread($update_runnable);
  }
});
$activity->setEventCallbacks($callbacks);

my $button_click_event = sub {
  my ($invocation_handler, $this, $method_info, $args) = @_;
  my $method_name = $method_info->[2];

  if($method_name eq 'onClick') {
    print "I was clicked!\n";

    my $file_intent = android::content::Intent->new::on($conn, 'org.openintents.action.PICK_FILE');
    $activity->startActivityForResult($file_intent, $PICK_FILE_RESULT);
  }
};

my $button_event_listener = uk::me::desert_island::theorbtwo::bridge::InterfaceImplementor->can::on($conn, 'make_instance')->('android.view.View$OnClickListener', $button_click_event);
$button->setOnClickListener($button_event_listener);

my $runnable = uk::me::desert_island::theorbtwo::bridge::MethodCallRunnable->new::on($conn, $activity, 'setContentView', [$layout]);
$activity->runOnUiThread($runnable);

# Object::Remote::MiniLoop
my $or_loop = Object::Remote->current_loop;
$or_loop->want_run;
$or_loop->run_while_wanted;



