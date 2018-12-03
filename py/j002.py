# coding: utf-8
get_ipython().run_line_magic('edit', 'p002.py')
get_ipython().run_line_magic('edit', '')
get_ipython().run_line_magic('edit', '')
get_ipython().run_line_magic('edit', 'p002.py')
get_ipython().run_line_magic('run', 'p002.py')
get_ipython().run_line_magic('ls', '')
get_ipython().run_line_magic('cd', '../py/')
get_ipython().run_line_magic('ls', '')
get_ipython().run_line_magic('run', 'p002.py')
get_ipython().run_line_magic('run', 'p002.py')
all_lines
map(ord, all_lines)
[list(map(ord, line)) for line in all_lines]
foo = np.array([list(map(ord, line)) for line in all_lines])
import numpy as np
foo = np.array([list(map(ord, line)) for line in all_lines])
foo
foo.shape
diffs = (foo[:,None,:] - foo[None,:,:])
diffs
diffs.shape
diffslots = (np.abs(diffs) > 0).sum(-1)
diffslots.shape
diffslots.min()
diffslots[diffslots==1]
diffslots.ravel()==1
(diffslots.ravel()==1).sum()
np.where(diffslots==1)
all_lines[77]
all_lines[128]
foo = _29
bar = _30
foo.split() == bar.split()
[a == b for (a,b) in zip(foo.split(), bar.split())]
foo
foo.split()
foo
list(foo)
list(foo) == list(bar)
[a==b for a,b in zip(list(foo),list(bar))]
[a for a,b in zip(list(foo),list(bar)) if a==b]
''.join([a for a,b in zip(list(foo),list(bar)) if a==b])
get_ipython().run_line_magic('save', 'j002 ~0/')
