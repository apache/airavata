package main

import (
"fmt"
)

func main() {
	slice := []int{1,3,4,5}
	vf(slice...)
}

func vf(a ...int)  int {
	if len(a)==0 {
		return 0
	}
	var x int
	for _, v := range a {
		x= fmt.Println(v)
	}
	return  x
}
