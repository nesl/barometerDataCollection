// ISensingService.aidl
package ucla.nesl.pressuresensing2;

import ucla.nesl.pressuresensing2.DisplayPack;

interface ISensingService {
    DisplayPack getDisplayPack();
    String getPathRoot();
}
