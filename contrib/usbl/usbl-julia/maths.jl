"""
    arrival_vector(needle, haystacks, receiver_offsets)

Determine a least squares estimate of `u/c` where `u` is the unit vector
pointing towards the transmitter and `c` is the sound speed in units derived
from those of `arrival_times` and `receiver_offsets`.
"""
function arrival_vector(needle, haystacks, receiver_offsets)
    t = arrival_times(needle, haystacks)
    dt = t[2:end] .- t[1]
    return -adjoint(receiver_offsets) \ dt
end

"""
    arrival_times(needle, haystacks; max_echo = 2.0)

Determine the fractional index of the first occurrence of `needle` in each
column of `haystacks`.

The returned index is one-based and may be non-positive if the tail of the
needle correlates well with the head of the haystack.

# Examples
```
julia> arrival_times([1], [0,0,1,0])
1-element Vector{Float64}:
 3.0

julia> arrival_times([1], [0,0,0.5,0.5])
1-element Vector{Float64}:
 3.5

julia> # First occurrence is ignored as noise
       arrival_times([1], [1; zeros(999); 2; zeros(999)]; max_echo = 1.99)
1-element Vector{Float64}:
 1001.0

julia> # First occurrence is accepted as true first arrival
       arrival_times([1], [1; zeros(999); 2; zeros(999)]; max_echo = 2.01)
1-element Vector{Float64}:
 1.0

julia> arrival_times([0,1], [1,0,0,0])
1-element Vector{Float64}:
 0.0
```
"""
function arrival_times(needle, haystacks; max_echo = 2.0)
    # Subsampling when evaluating cross-correlations.
    # The current value of 8 resolves the peaks of the highest frequency
    # component up to a fraction of at least `cos(π/16) ≈ 0.98`.
    fine_factor = 8

    T = float(real(promote_type(eltype(needle), eltype(haystacks))))
    n_coarse = nextprod((2, 3, 5, 7), length(needle) + size(haystacks, 1) - 1)
    coarse_vec = Vector{complex(T)}(undef, n_coarse)
    fine_vec = Vector{complex(T)}(undef, fine_factor * n_coarse)

    padded_needle = shift_pad(complex(T), needle, n_coarse, 0)
    fft_needle = fft!(padded_needle)

    return [
        begin
            # Compute cross-correlation
            padded_haystack = shift_pad!(coarse_vec, haystack, length(needle) - 1)
            fft_haystack = fft!(padded_haystack)
            fft_correlation_coarse = (coarse_vec .= conj.(fft_needle) .* fft_haystack)
            fft_correlation_fine = fft_pad!(fine_vec, fft_correlation_coarse)
            correlation = ifft!(fft_correlation_fine)

            # Determine the magnitude of the strongest arrival
            max_correlation2 = maximum(abs2, correlation)

            # Determine the index of the first correlation that is at most
            # `max_echo` times weaker than the strongest correlation
            i = findfirst(correlation -> max_echo^2 * abs2(correlation) >= max_correlation2, correlation)

            # Keep going until we reach the next peak
            while (i < length(correlation) && abs2(correlation[i]) < abs2(correlation[i+1]))
                i += 1
            end

            # Convert correlation index to (fractional) index into haystack
            (i - 1) / fine_factor - length(needle) + 2
        end for haystack in eachcol(haystacks)
    ]
end

"""
    fft_pad([T,] x, n)

Pad the Fourier coefficients `x` to length `n` by adding higher frequencies of
magnitude zero. This corresponds to interpolation in the time domain.

The optional first argument `T` specifies the type of the output vector.

If `length(x)` is even, then the highest-frequency component is split into two
components of half the magnitude traveling in opposite directions. This ensures
that real input signals remain real after padding.
"""
function fft_pad end
fft_pad(x, n) = fft_pad(eltype(x), x, n)
fft_pad(T::Type, x, n) = fft_pad!(zeros(T, n), x)

"""
    fft_pad!(y, x)

In-place version of `fft_pad()`.
"""
function fft_pad!(y, x)
    @assert length(x) <= length(y)
    if length(x) == length(y)
        return y .= x
    end
    n = fld(length(x) - 1, 2)
    y[1:n+1] .= x[1:n+1]
    if iseven(length(x))
        xx = x[n+2] / 2
        y[n+2] = xx
        y[end-n] = conj(xx)
    end
    y[n+2:end-n] .= 0
    y[end-n+1:end] .= x[end-n+1:end]
    return y
end

"""
    shift_pad([T,] x, n, i)

Allocate an all-zero vector of length `n`, then copy the elements of `x` into
it starting at index `1+i` (i.e. shifted by `i` elements).

The optional first argument `T` specifies the type of the output vector.
"""
function shift_pad end
function shift_pad(T::Type, x::AbstractVector, n::Integer, i::Integer)
    y = zeros(T, n)
    y[i.+(1:length(x))] .= x
    return y
end

"""
    shift_pad!(y, x, i)

In-place version of `shift_pad()`.
"""
function shift_pad!(y, x, i)
    y .= 0
    y[i.+(1:length(x))] .= x
    return y
end

"""
    angles_from_vector(u) -> (azimuth, elevation)

Azimuth and elevation of the given vector. 

The input `u` is not assumed to be a unit vector. The angles are returned in
degrees and according to the following conventions.
- Azimuth: 0° is the (+x,z)-plane, 90° is the (+y,z)-plane.
- Elevation: 0° is the xy-plane, 90° is the +z-axis.
"""
function angles_from_vector(u)
    azimuth = atand(u[2], u[1])
    elevation = atand(u[3], norm(u[1:2]))
    return (azimuth, elevation)
end