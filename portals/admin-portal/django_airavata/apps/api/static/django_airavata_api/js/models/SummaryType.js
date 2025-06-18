import BaseEnum from "./BaseEnum";

export default class SummaryType extends BaseEnum {}
SummaryType.init(["SSH", "PASSWD", "CERT"], true);
