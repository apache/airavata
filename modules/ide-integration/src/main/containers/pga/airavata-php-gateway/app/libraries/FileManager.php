<?php

class FileManager {
    /**
     * returns a json message ether successful or failure.
     */
    public static function msg($status, $msg) {
        return json_encode(array(
            'status' => $status,
            'msg' => $msg
        ));
    }

    /**
     * Takes a given path and prints the content in json format.
     */
    public static function get_content($dataRoot, $path) {
        $path = $dataRoot . $path;
        $path = rtrim($path, '/');

        // get dir content
        $files = array();
        $folders = array();
        FileManager::list_dir($path, $files, $folders);
        $files = array_merge($folders, $files);

        // get info
        foreach ($files as $k => $v) {
            $i = FileManager::get_file_info($v['path'], array(
                'name',
                'size',
                'date',
                'fileperms'
            ));

            if ($v['folder']) {
                $files[$k] = array (
                    'name' => $i['name'],
                    'size' => '---',
                    'date' => date('Y-m-d H:i:s', $i['date']),
                    'perm' => FileManager::unix_perm_string($i['fileperms']),
                    'folder' => True
                );
            } else {
                $files[$k] = array(
                    'name' => $i['name'],
                    'size' => FileManager::human_filesize($i['size']),
                    'date' => date('Y-m-d H:i:s', $i['date']),
                    'perm' => FileManager::unix_perm_string($i['fileperms']),
                    'folder' => False
                );
            }
            $files[$k]['link'] = str_replace($dataRoot, '', $v['path']);
        }

        return json_encode(array('status' => 'ok', 'files' => $files));
    }

    /**
     * returns html to build a breadcrumb based upon path.
     * path should look like this: page/subdir/gfx
     */
    public static function breadcrumb($path) {
        $parts = explode('/', $path);
        $html = '';
        $link = '';
        for ($i = 0; $i < count($parts); $i++) {
            if ($i != count($parts) - 1) {
                $link .= $i == 0 ? $parts[$i] : '/'.$parts[$i];
                $html .= "<li><a href=\"$link\">".$parts[$i].'</a></li>';
            } else {
                $html .= '<li class="active">'.$parts[$i].'</li>';
            }
        }

        return "<ol class=\"breadcrumb\">$html</ol>";
    }

    /**
     * stores files and folders of given directory in the provided arrays.
     */
    private static function list_dir($path, &$files, &$folders) {
        foreach (scandir($path) as $f) {
            if ($f == '.' || $f == '..') continue;

            $file = array(
                'name' => $f,
                'path' => "$path/$f",
            );

            if (is_dir($file['path'])) {
                $file['folder'] = true;
                $folders[] = $file;
            } else {
                $file['folder'] = false;
                $files[] = $file;
            }
        }
    }

    /**
     * create thumbnail. will return false on failure.
     */
    private static function create_thumbnail($picture, $picture_thumb, $w) {
        // get src image
        if (pathinfo($picture, PATHINFO_EXTENSION) == "jpg")
            $source_image = imagecreatefromjpeg($picture);
        else if (pathinfo($picture, PATHINFO_EXTENSION) == "png")
            $source_image = imagecreatefrompng($picture);
        else
            return false;

        $width = imagesx($source_image);
        $height = imagesy($source_image);

        // calc height according to given width
        $h = floor($height * ($w / $width));

        // create virtual
        $virtual_image = imagecreatetruecolor($w, $h);

        // copy src image
        imagecopyresized($virtual_image, $source_image, 0, 0, 0, 0, $w, $h, $width, $height);

        // create thumbnail
        if (pathinfo($picture, PATHINFO_EXTENSION) == 'jpg')
            imagejpeg($virtual_image, $picture_thumb, 83);
        elseif (pathinfo($picture, PATHINFO_EXTENSION) == 'png')
            imagepng($virtual_image, $picture_thumb);

        return true;
    }

    /**
     * returns a password hash.
     */
    private static function generate_hash($pass, $salt) {
        return hash('sha256', $pass.$salt);
    }

    /**
     * returns a random salt.
     */
    private static function generate_salt() {
        mt_srand(microtime(true) * 100000 + memory_get_usage(true));
        return uniqid(mt_rand(), true);
    }

    /**
     * Takes a file size in bytes and returns a human readable filesize.
     */
    private static function human_filesize($bytes, $decimals = 2) {
        $sz = 'BKMGTP';
        $factor = floor((strlen($bytes) - 1) / 3);
        return sprintf("%.{$decimals}f", $bytes / pow(1024, $factor)) . @$sz[$factor];
    }

    /**
     * Takes an permission value and builds a human readable unix style permission
     * string.
     * Taken from php.net.
     */
    private static function unix_perm_string($perms) {
        if (($perms & 0xC000) == 0xC000) {
            // Socket
            $info = 's';
        } elseif (($perms & 0xA000) == 0xA000) {
            // Symbolic Link
            $info = 'l';
        } elseif (($perms & 0x8000) == 0x8000) {
            // Regular
            $info = '-';
        } elseif (($perms & 0x6000) == 0x6000) {
            // Block special
            $info = 'b';
        } elseif (($perms & 0x4000) == 0x4000) {
            // Directory
            $info = 'd';
        } elseif (($perms & 0x2000) == 0x2000) {
            // Character special
            $info = 'c';
        } elseif (($perms & 0x1000) == 0x1000) {
            // FIFO pipe
            $info = 'p';
        } else {
            // Unknown
            $info = 'u';
        }

        // Owner
        $info .= (($perms & 0x0100) ? 'r' : '-');
        $info .= (($perms & 0x0080) ? 'w' : '-');
        $info .= (($perms & 0x0040) ?
            (($perms & 0x0800) ? 's' : 'x' ) :
            (($perms & 0x0800) ? 'S' : '-'));

        // Group
        $info .= (($perms & 0x0020) ? 'r' : '-');
        $info .= (($perms & 0x0010) ? 'w' : '-');
        $info .= (($perms & 0x0008) ?
            (($perms & 0x0400) ? 's' : 'x' ) :
            (($perms & 0x0400) ? 'S' : '-'));

        // World
        $info .= (($perms & 0x0004) ? 'r' : '-');
        $info .= (($perms & 0x0002) ? 'w' : '-');
        $info .= (($perms & 0x0001) ?
            (($perms & 0x0200) ? 't' : 'x' ) :
            (($perms & 0x0200) ? 'T' : '-'));

        return $info;
    }

    /**
     * Returns information about a given file.
     * Taken from code igniter.
     */
    private static function get_file_info($file, $returned_values = array('name', 'server_path', 'size', 'date')) {
        if (!file_exists($file)) {
            return FALSE;
        }

        if (is_string($returned_values)) {
            $returned_values = explode(',', $returned_values);
        }

        foreach ($returned_values as $key) {
            switch ($key) {
                case 'name':
                    $fileinfo['name'] = substr(strrchr($file, DIRECTORY_SEPARATOR), 1);
                    if( $fileinfo['name'] == false)
                        $fileinfo['name'] = substr(strrchr($file, "/"), 1);
                    break;
                case 'server_path':
                    $fileinfo['server_path'] = $file;
                    break;
                case 'size':
                    $fileinfo['size'] = filesize($file);
                    break;
                case 'date':
                    $fileinfo['date'] = filemtime($file);
                    break;
                case 'readable':
                    $fileinfo['readable'] = is_readable($file);
                    break;
                case 'writable':
                    // There are known problems using is_weritable on IIS.  It may not be reliable - consider fileperms()
                    $fileinfo['writable'] = is_writable($file);
                    break;
                case 'executable':
                    $fileinfo['executable'] = is_executable($file);
                    break;
                case 'fileperms':
                    $fileinfo['fileperms'] = fileperms($file);
                    break;
            }
        }

        return $fileinfo;
    }

}