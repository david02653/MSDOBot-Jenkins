# This files contains your custom actions which can be used to run
# custom Python code.
#
# See this guide on how to implement these action:
# https://rasa.com/docs/rasa/custom-actions


# This is a simple example for a custom action which utters "Hello World!"

from typing import Any, Text, Dict, List

from rasa_sdk.events import SlotSet
from rasa_sdk import Action, Tracker
from rasa_sdk.executor import CollectingDispatcher
#
#
# class ActionHelloWorld(Action):
#
#     def name(self) -> Text:
#         return "action_hello_world"
#
#     def run(self, dispatcher: CollectingDispatcher,
#             tracker: Tracker,
#             domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
#
#         dispatcher.utter_message(text="Hello World!")
#
#         return []

class ActionGreet(Action):
    def name(self) -> Text:
        return "action_greet"
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            msg = {
                "intent": "greet"
            }
            dispatcher.utter_message(format(msg))
            return []

class ActionBotHelp(Action):

    def name(self) -> Text:
        return "action_bot_help"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            msg = {
                "intent": "help"
            }
            dispatcher.utter_message(format(msg))
            return []

class ActionJobViewList(Action):

    def name(self) -> Text:
        return "action_job_view_list"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            msg = {
                "intent": "ask_job_view_list"
            }
            dispatcher.utter_message(format(msg))
            return []

class ActionJobViewDetail(Action):

    def name(self) -> Text:
        return "action_job_view_detail"
    
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            # set jobName as None if no entity is found
            jobName = next(tracker.get_latest_entity_values("job_name"), None)
            lostName = False
            if jobName == None:
                lostName = True
                jobName = tracker.get_slot("lastJob")
            msg = {
                "intent": "ask_view_detail",
                "jobName": jobName,
                "lostName": lostName
            }
            dispatcher.utter_message(format(msg))
            return [SlotSet("lastJob", jobName)]

class ActionJobList(Action):
    
    def name(self) -> Text:
        return "action_job_list"
    
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            msg = {
                "intent": "ask_job_list"
            }
            dispatcher.utter_message(format(msg))
            return []

class ActionTestReport(Action):

    def name(self) -> Text:
        return "action_job_test_report"
    
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            jobName = next(tracker.get_latest_entity_values("job_name"), None)
            lostName = False
            if jobName == None:
                lostName = True
                jobName = tracker.get_slot("lastJob")
            msg = {
                "intent": "ask_job_test_report",
                "jobName": jobName,
                "lostName": lostName
            }
            dispatcher.utter_message(format(msg))
            return [SlotSet("lastJob", jobName)]

class ActionHealthReport(Action):
    
    def name(self) -> Text:
        return "action_job_health_report"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            jobName = next(tracker.get_latest_entity_values("job_name"), None)
            lostName = False
            if jobName == None:
                lostName = True
                jobName = tracker.get_slot("lastJob")
            msg = {
                "intent": "ask_job_health_report",
                "jobName": jobName,
                "lostName": lostName
            }
            dispatcher.utter_message(format(msg))
            return [SlotSet("lastJob", jobName)]

class ActionBuildResult(Action):
    
    def name(self) -> Text:
        return "action_job_build_result"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            jobName = next(tracker.get_latest_entity_values("job_name"), None)
            lostName = False
            if jobName == None:
                lostName = True
                jobName = tracker.get_slot("lastJob")
            msg = {
                "intent": "ask_build_result",
                "jobName": jobName,
                "lostName": lostName
            }
            dispatcher.utter_message(format(msg))
            return [SlotSet("lastJob", jobName)]

class ActionJobLastBuildReport(Action):
    
    def name(self) -> Text:
        return "action_job_last_build_report"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            jobName = next(tracker.get_latest_entity_values("job_name"), None)
            lostName = False
            if jobName == None:
                lostName = True
                jobName = tracker.get_slot("lastJob")
            msg = {
                "intent": "ask_last_build_report",
                "jobName": jobName,
                "lostName": lostName
            }
            dispatcher.utter_message(format(msg))
            return [SlotSet("lastJob", jobName)]

class ActionJobGitUpdate(Action):
    
    def name(self) -> Text:
        return "action_job_git_update"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            jobName = next(tracker.get_latest_entity_values("job_name"), None)
            lostName = False
            if jobName == None:
                lostName = True
                jobName = tracker.get_slot("lastJob")
            msg = {
                "intent": "ask_job_git_update",
                "jobName": jobName,
                "lostName": lostName
            }
            dispatcher.utter_message(format(msg))
            return [SlotSet("lastJob", jobName)]

class ActionSystemLatestBuild(Action):
    
    def name(self) -> Text:
        return "action_system_latest_build"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            msg = {
                "intent": "ask_system_latest_build"
            }
            dispatcher.utter_message(format(msg))
            return []

class ActionSystemFailedBuild(Action):
    
    def name(self) -> Text:
        return "action_system_failed_build"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            msg = {
                "intent": "ask_system_failed_build"
            }
            dispatcher.utter_message(format(msg))
            return []

class ActionSystemAllBuild(Action):
    
    def name(self) -> Text:
        return "action_system_all_build"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            msg = {
                "intent": "ask_system_all_build"
            }
            dispatcher.utter_message(format(msg))
            return []

class ActionJenkinsLog(Action):
    
    def name(self) -> Text:
        return "action_jenkins_log"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            msg = {
                "intent": "ask_jenkins_log"
            }
            dispatcher.utter_message(format(msg))
            return []

class ActionSystemSevereLog(Action):
    
    def name(self) -> Text:
        return "action_jenkins_severe_log"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            msg = {
                "intent": "ask_jenkins_severe_log"
            }
            dispatcher.utter_message(format(msg))
            return []

class ActionJenkinsWarningLog(Action):
    
    def name(self) -> Text:
        return "action_jenkins_warning_log"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            msg = {
                "intent": "ask_jenkins_warning_log"
            }
            dispatcher.utter_message(format(msg))
            return []

class ActionJenkinsPluginInfo(Action):
    
    def name(self) -> Text:
        return "action_plugin_info"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            msg = {
                "intent": "ask_plugin_info"
            }
            dispatcher.utter_message(format(msg))
            return []

class ActionJenkinsEnvInfo(Action):
    
    def name(self) -> Text:
        return "action_env_info"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            msg = {
                "intent": "ask_env_info"
            }
            dispatcher.utter_message(format(msg))
            return []

class ActionJenkinsCredentialInfo(Action):
    
    def name(self) -> Text:
        return "action_credential_info"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            msg = {
                "intent": "ask_credential_info"
            }
            dispatcher.utter_message(format(msg))
            return []

class ActionViewTestReportOverview(Action):
    def name(self) -> Text:
        return "action_view_test_report_overview"
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
            jobName = next(tracker.get_latest_entity_values("job_name"), None)
            lostName = False
            if jobName == None:
                lostName = True
                jobName = tracker.get_slot("lastJob")
            msg = {
                "intent": "ask_view_test_report_overview",
                "jobName": jobName,
                "lostName": lostName
            }
            dispatcher.utter_message(format(msg))
            return [SlotSet("lastJob", jobName)]