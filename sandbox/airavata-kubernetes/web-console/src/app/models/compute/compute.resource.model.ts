/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
export class ComputeResource {

  id: number;
  name: string;
  host: string;
  userName: string;
  password: string;
  communicationType: string;

  constructor(id: number = 0, name: string = null, host: string = null, userName: string = null,
              password: string = null, communicationType: string = "Mock") {
    this.id = id;
    this.name = name;
    this.host = host;
    this.userName = userName;
    this.password = password;
    this.communicationType = communicationType;
  }

  public static fromJson(json:any): ComputeResource {
    return new ComputeResource(json.id, json.name, json.host, json.userName,
      json.password, json.communicationType);
  }
}
