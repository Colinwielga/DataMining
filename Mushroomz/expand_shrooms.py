import sys

def main():
	attr_map = []
	with open("../Downloads/mushrooms.names") as f:
		for line in f:
			new_map = {}
			for attrVal in (attrVal.split("=") for attrVal in line.split(": ")[1].split(".")[0].split(", ")):
				new_map[attrVal[1]] = attrVal[0]
			attr_map.append(new_map)
	with open("../Downloads/agaricus-lepiota.data") as f:
		for line in f:
			fields = line.rstrip().split(",")
			for i in xrange(len(fields)):
				sys.stdout.write(attr_map[i][fields[i]])
				if i == len(fields) - 1:
					print
				else:
					print ",",

if __name__ == '__main__':
	main()

