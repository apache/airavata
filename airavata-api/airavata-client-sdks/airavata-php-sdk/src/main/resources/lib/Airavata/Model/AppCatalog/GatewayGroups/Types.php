<?php
namespace Airavata\Model\AppCatalog\GatewayGroups;

/**
 * Autogenerated by Thrift Compiler (0.10.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
use Thrift\Base\TBase;
use Thrift\Type\TType;
use Thrift\Type\TMessageType;
use Thrift\Exception\TException;
use Thrift\Exception\TProtocolException;
use Thrift\Protocol\TProtocol;
use Thrift\Protocol\TBinaryProtocolAccelerated;
use Thrift\Exception\TApplicationException;


class GatewayGroups {
  static $_TSPEC;

  /**
   * @var string
   */
  public $gatewayId = null;
  /**
   * @var string
   */
  public $adminsGroupId = null;
  /**
   * @var string
   */
  public $readOnlyAdminsGroupId = null;
  /**
   * @var string
   */
  public $defaultGatewayUsersGroupId = null;

  public function __construct($vals=null) {
    if (!isset(self::$_TSPEC)) {
      self::$_TSPEC = array(
        1 => array(
          'var' => 'gatewayId',
          'type' => TType::STRING,
          ),
        2 => array(
          'var' => 'adminsGroupId',
          'type' => TType::STRING,
          ),
        3 => array(
          'var' => 'readOnlyAdminsGroupId',
          'type' => TType::STRING,
          ),
        4 => array(
          'var' => 'defaultGatewayUsersGroupId',
          'type' => TType::STRING,
          ),
        );
    }
    if (is_array($vals)) {
      if (isset($vals['gatewayId'])) {
        $this->gatewayId = $vals['gatewayId'];
      }
      if (isset($vals['adminsGroupId'])) {
        $this->adminsGroupId = $vals['adminsGroupId'];
      }
      if (isset($vals['readOnlyAdminsGroupId'])) {
        $this->readOnlyAdminsGroupId = $vals['readOnlyAdminsGroupId'];
      }
      if (isset($vals['defaultGatewayUsersGroupId'])) {
        $this->defaultGatewayUsersGroupId = $vals['defaultGatewayUsersGroupId'];
      }
    }
  }

  public function getName() {
    return 'GatewayGroups';
  }

  public function read($input)
  {
    $xfer = 0;
    $fname = null;
    $ftype = 0;
    $fid = 0;
    $xfer += $input->readStructBegin($fname);
    while (true)
    {
      $xfer += $input->readFieldBegin($fname, $ftype, $fid);
      if ($ftype == TType::STOP) {
        break;
      }
      switch ($fid)
      {
        case 1:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->gatewayId);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 2:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->adminsGroupId);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 3:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->readOnlyAdminsGroupId);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        case 4:
          if ($ftype == TType::STRING) {
            $xfer += $input->readString($this->defaultGatewayUsersGroupId);
          } else {
            $xfer += $input->skip($ftype);
          }
          break;
        default:
          $xfer += $input->skip($ftype);
          break;
      }
      $xfer += $input->readFieldEnd();
    }
    $xfer += $input->readStructEnd();
    return $xfer;
  }

  public function write($output) {
    $xfer = 0;
    $xfer += $output->writeStructBegin('GatewayGroups');
    if ($this->gatewayId !== null) {
      $xfer += $output->writeFieldBegin('gatewayId', TType::STRING, 1);
      $xfer += $output->writeString($this->gatewayId);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->adminsGroupId !== null) {
      $xfer += $output->writeFieldBegin('adminsGroupId', TType::STRING, 2);
      $xfer += $output->writeString($this->adminsGroupId);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->readOnlyAdminsGroupId !== null) {
      $xfer += $output->writeFieldBegin('readOnlyAdminsGroupId', TType::STRING, 3);
      $xfer += $output->writeString($this->readOnlyAdminsGroupId);
      $xfer += $output->writeFieldEnd();
    }
    if ($this->defaultGatewayUsersGroupId !== null) {
      $xfer += $output->writeFieldBegin('defaultGatewayUsersGroupId', TType::STRING, 4);
      $xfer += $output->writeString($this->defaultGatewayUsersGroupId);
      $xfer += $output->writeFieldEnd();
    }
    $xfer += $output->writeFieldStop();
    $xfer += $output->writeStructEnd();
    return $xfer;
  }

}

